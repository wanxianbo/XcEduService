package com.xuecheng.manage_media.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import com.xuecheng.manage_media.service.IMediaUploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@Service
public class MediaUploadService implements IMediaUploadService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    private String upload_location;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routingkey_media_video;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //得到文件目录路径
    private String getFileFolderPath(String fileMd5) {
        return upload_location + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
    }

    //得到文件目录路径
    private String getFilePath(String fileMd5, String fileExt) {
        return upload_location + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
    }

    //得到分块文件目录路径
    private String getChunkFileFolderPath(String fileMd5) {
        return upload_location + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/chunk/";
    }

    private boolean createFileFold(String fileMd5) {
        String fileFolderPath = getFileFolderPath(fileMd5);
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            return fileFolder.mkdirs();
        }
        return true;
    }
    //文件上传注册
    @Override
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //检查文件是否上传
        //1、得到文件的路径
        String filePath = getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        //2、查询数据库文件是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (file.exists() && optional.isPresent()) {
            throw new CustomerException(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        boolean fileFold = createFileFold(fileMd5);
        if (!fileFold) {
            //上传文件目录创建失败
            throw new CustomerException(MediaCode.UPLOAD_FILE_REGISTER_CREATEFOLDER_FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //分块检查
    @Override
    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //得到块文件所在路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //块文件的文件名称以1,2,3..序号命名，没有扩展名
        File chunkFile = new File(chunkFileFolderPath + chunk);
        if (chunkFile.exists()) {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        }
        return new CheckChunkResult(MediaCode.CHUNK_FILE_NOT_EXIST_CHECK,false);
    }

    //分块上传
    @Override
    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5) {
        //得到分块目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //定义块文件路径
        String chunkFile = chunkFileFolderPath + chunk;
        //判断目录存在否
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = file.getInputStream();
            fileOutputStream = new FileOutputStream(new File(chunkFile));
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (IOException e) {
            log.error("upload chunk file fail:{}",e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //得到分块文件
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        //得到合并文件目录
        String filePath = getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        try {
            boolean newFile = mergeFile.createNewFile();
        } catch (IOException e) {
            log.error("mergeChunks..create mergeFile fail:{}",e.getMessage());
            throw new CustomerException(MediaCode.MERGE_FILE_FAIL);
        }
        //获取分块文件列表
        File[] files = chunkFileFolder.listFiles();
        if (files == null) {
            throw new CustomerException(MediaCode.CHUNK_FILE_NOT_EXIST_CHECK);
        }
        List<File> fileList = Arrays.asList(files);
        //文件List排序
        Collections.sort(fileList,(o1, o2) -> {
            if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                return 1;
            }
            return -1;
        });
        //执行合并
        mergeFile = mergeFile(mergeFile, fileList);
        if (mergeFile == null) {
            throw new CustomerException(MediaCode.MERGE_FILE_FAIL);
        }
        //验证文件的MD5值
        boolean checkResult = checkFileMd5(mergeFile, fileMd5);
        if (!checkResult) {
            throw new CustomerException(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //保存到数据库
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        mediaFile.setFilePath(fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/");
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态改为已上传
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        //发送消息
        sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private ResponseResult sendProcessVideoMsg(String mediaId) {
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }
        MediaFile mediaFile = optional.get();
        //发送视频处理消息
        Map<String, String> map = new HashMap<>();
        map.put("mediaId", mediaFile.getFileId());
        String msg = JSON.toJSONString(map);
        //发送消息
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, msg);
            log.info("send media process task msg:{}",msg);
        } catch (Exception e) {
            log.info("send media process task error,msg is:{},error:{}", msg, e.getMessage());
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //验证文件的MD5值
    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        if (mergeFile == null) {
            return false;
        }
        //进行md5校验
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(mergeFile);
            String md5Hex = DigestUtils.md5DigestAsHex(inputStream);
            if (StringUtils.equalsIgnoreCase(fileMd5, md5Hex)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("checkFileMd5 error,file is:{},fileMd5 is: {}",mergeFile.getAbsoluteFile(),fileMd5);
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //合并文件
    private File mergeFile(File mergeFile, List<File> fileList) {
        try {
            //创建写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            //缓冲区
            byte[] bytes = new byte[1024];
            for (File chunkFile : fileList) {
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
                int len = -1;
                while ((len = raf_read.read(bytes)) != -1) {
                    //向合并文件中写数据
                    raf_write.write(bytes, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("merge file error:{}",e.getMessage());
            return null;
        }
        return mergeFile;
    }
}
