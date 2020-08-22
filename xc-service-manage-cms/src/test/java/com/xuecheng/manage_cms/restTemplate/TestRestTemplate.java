package com.xuecheng.manage_cms.restTemplate;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.service.ICmsTemplateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ICmsTemplateService iCmsTemplateService;

    @Test
    public void testRestTemplate(){
        ResponseEntity<CmsConfig> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", CmsConfig.class);
        System.out.println(forEntity.getBody());
    }

    @Test
    public void testGetHtml(){
        String html = iCmsTemplateService.getPageHtml("5f199da287127013666362aa");
        System.out.println(html);
    }
}
