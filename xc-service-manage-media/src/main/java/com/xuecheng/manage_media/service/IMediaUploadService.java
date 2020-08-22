package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

public interface IMediaUploadService {

    ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

    CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize);

    ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5);

    ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);
}
