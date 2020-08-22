package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "文件管理接口", description = "文件管理接口，提供文件的上传、下载、查")
public interface FileSystemControllerApi {

    /**
     * 上传文件
     * @param file 文件
     * @param filetag 文件标签
     * @param businesskey 业务key
     * @param metadata 元信息,json格式
     * @return
     */
    @ApiOperation("文件上传")
    UploadFileResult upload(MultipartFile file,
                            String filetag,
                            String businesskey,
                            String metadata);
}
