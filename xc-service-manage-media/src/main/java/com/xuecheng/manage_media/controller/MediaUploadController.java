package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaUploadControllerApi;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.IMediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media/upload")
public class MediaUploadController implements MediaUploadControllerApi {


    @Autowired
    private IMediaUploadService mediaUploadService;

    //上传前检查上传环境
    @Override
    @PostMapping("/register")
    public ResponseResult register(@RequestParam("fileMd5") String fileMd5,
                                   @RequestParam("fileName") String fileName,
                                   @RequestParam("fileSize") Long fileSize,
                                   @RequestParam("mimetype") String mimetype,
                                   @RequestParam("fileExt") String fileExt) {
        return mediaUploadService.register(fileMd5, fileName, fileSize, mimetype, fileExt);
    }

    //分块检查
    @Override
    @PostMapping("/checkchunk")
    public CheckChunkResult checkChunk(@RequestParam("fileMd5") String fileMd5,
                                       @RequestParam("chunk") Integer chunk,
                                       @RequestParam("chunkSize") Integer chunkSize) {
        return mediaUploadService.checkChunk(fileMd5, chunk, chunkSize);
    }

    //上传分块
    @Override
    @PostMapping("/uploadchunk")
    public ResponseResult uploadChunk(@RequestParam("file") MultipartFile file,
                                      @RequestParam("chunk") Integer chunk,
                                      @RequestParam("fileMd5") String fileMd5) {
        return mediaUploadService.uploadChunk(file, chunk, fileMd5);
    }

    //文件合并
    @Override
    @PostMapping("/mergechunks")
    public ResponseResult mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                      @RequestParam("fileName") String fileName,
                                      @RequestParam("fileSize") Long fileSize,
                                      @RequestParam("mimetype") String mimetype,
                                      @RequestParam("fileExt") String fileExt) {
        return mediaUploadService.mergeChunks(fileMd5, fileName, fileSize, mimetype, fileExt);
    }
}
