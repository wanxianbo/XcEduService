package com.xuecheng.auth.client;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "xc-service-ucenter")
@RequestMapping("/ucenter")
public interface UserClient {
    @GetMapping("/getuserext")
    XcUserExt getUserExt(@RequestParam("username") String username);
}
