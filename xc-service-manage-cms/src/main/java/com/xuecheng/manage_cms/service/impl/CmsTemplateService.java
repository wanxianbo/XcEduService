package com.xuecheng.manage_cms.service.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import com.xuecheng.manage_cms.service.ICmsTemplateService;
import com.xuecheng.manage_cms.service.IPageService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class CmsTemplateService implements ICmsTemplateService {

    @Autowired
    private IPageService pageService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;

    @Override
    public String getPageHtml(String pageId) {
        //获取页面模型数据
        Map model = getModelByPageId(pageId);
        if (CollectionUtils.isEmpty(model)) {
            throw new CustomerException(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板
        String templateContent = getTemplateByPageId(pageId);
        if (StringUtils.isBlank(templateContent)) {
            throw new CustomerException(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化
        String html = generateHtml(templateContent, model);
        if (StringUtils.isBlank(html)) {
            throw new CustomerException(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    //生产静态html
    private String generateHtml(String templateContent, Map model) {
        //生产配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        //配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板
        try {
            Template template = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //获取页面模板
    private String  getTemplateByPageId(String pageId) {
        //获取页面
        CmsPage cmsPage = pageService.findById(pageId);
        if (cmsPage == null) {
            throw new CustomerException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //在获取模板id
        String templateId = cmsPage.getTemplateId();
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //获取文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //取出模板文件内容
            //根据id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            if (gridFSFile != null) {
                GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
                //创建gridFsResource，用于获取流对象
                GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
                //获取流中的数据
                try {
                    String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                    return content;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //获取模板
        return null;
    }

    //获取页面数据
    private Map getModelByPageId(String pageId) {
        CmsPage cmsPage = pageService.findById(pageId);
        if (cmsPage == null) {
            throw new CustomerException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isBlank(dataUrl)) {
            throw new CustomerException(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate远程调用获取数据model
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        return forEntity.getBody();
    }
}
