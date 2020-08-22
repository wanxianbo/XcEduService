package com.xuecheng;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.test.freemarker.FreemarkerTestApplication;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FreemarkerTestApplication.class)
public class TestCreateTemplate {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        //创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //设置模板路径
        String classPath = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
        //设置字符集
        configuration.setDefaultEncoding("utf-8");
        //加载模板
        Template template = configuration.getTemplate("test1.ftlh");
        //数据模型
        Map<String, Object> map = new HashMap<>();
        map.put("name", "黑马程序员");
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        System.out.println(content);
        //输出文件
        //InputStream in = new ByteArrayInputStream(content.getBytes(), 0, content.getBytes().length);
        InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
        FileOutputStream fileOutputStream = new FileOutputStream(new File("e:/test1.html"));
        IOUtils.copy(inputStream, fileOutputStream);
    }

    @Test
    public void testGenerateHtmlByString() throws IOException, TemplateException {
        //创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //设置模板
        String templateString = "" +
                "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                " 名称：${name}\n" +
                " </body>\n" +
                "</html>";
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("template", templateString);
        configuration.setTemplateLoader(templateLoader);
        //设置字符集
        configuration.setDefaultEncoding("utf-8");
        //加载模板
        Template template = configuration.getTemplate("template");
        //数据模型
        Map<String, Object> map = new HashMap<>();
        map.put("name", "黑马程序员");
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        System.out.println(content);
        //输出文件
        InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
        FileOutputStream fileOutputStream = new FileOutputStream(new File("e:/test1.html"));
        IOUtils.copy(inputStream, fileOutputStream);
    }

    @Test
    public void testGridFs() throws FileNotFoundException {
        //要存储的文件
        File file = new File("e:/course.ftlh");
        //定义输入流
        FileInputStream inputStram = new FileInputStream(file);
        //向GridFS存储文件
        ObjectId objectId = gridFsTemplate.store(inputStram, "课程详情模板文件测试1", "utf-8");
        //得到文件ID
        String fileId = objectId.toString();
        System.out.println(fileId);
    }

    @Test
    public void queryFile() throws IOException {

    }

    //删除文件
    @Test
    public void testDelFile() throws IOException {
        //根据文件id删除fs.files和fs.chunks中的记录
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is("5f1bb08c467c6a7b0d675804")));
    }

}
