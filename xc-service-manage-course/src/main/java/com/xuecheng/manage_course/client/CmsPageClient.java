package com.xuecheng.manage_course.client;

import com.xuecheng.manage_cms.api.CmsPageApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "xc-service-manage-cms")
public interface CmsPageClient extends CmsPageApi {

}
