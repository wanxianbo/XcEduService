package om.xuecheng.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import lombok.extern.slf4j.Slf4j;
import om.xuecheng.dao.FileSystemRepository;
import om.xuecheng.service.IFileSystemService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class FileSystemService implements IFileSystemService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private FileSystemRepository fileSystemRepository;

    //上传文件
    @Override
    public UploadFileResult upload(MultipartFile file, String filetag, String businesskey, String metadata) {
        //检验文件
        if (file == null) {
            throw new CustomerException(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        //获取文件扩展名
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        try {
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            String fileId = storePath.getFullPath();
            //保存到MongoDB中
            //创建文件对象
            FileSystem fileSystem = new FileSystem();
            fileSystem.setFileId(fileId);
            fileSystem.setFilePath(fileId);
            fileSystem.setFiletag(filetag);
            fileSystem.setBusinesskey(businesskey);
            if (StringUtils.isNotBlank(metadata)) {
                Map map = JSON.parseObject(metadata, Map.class);
                fileSystem.setMetadata(map);
            }
            fileSystem.setFileName(file.getOriginalFilename());
            fileSystem.setFileSize(file.getSize());
            //文件类型
            fileSystem.setFileType(file.getContentType());
            fileSystemRepository.save(fileSystem);
            return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
        } catch (IOException e) {
            log.error("文件上传失败！");
            throw new CustomerException(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
    }

}
