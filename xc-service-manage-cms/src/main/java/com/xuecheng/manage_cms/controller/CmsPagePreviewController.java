package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.ICmsTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private ICmsTemplateService cmsTemplateService;

    @GetMapping("/cms/preview/{pageId}")
    public void preview(@PathVariable("pageId") String pageId) {
        String pageHtml = cmsTemplateService.getPageHtml(pageId);
        if (StringUtils.isNotBlank(pageHtml)) {
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                response.setContentType("text/html;charset=utf-8");
                //response.setHeader("Content‐type","text/html;charset=utf‐8");
                outputStream.write(pageHtml.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
