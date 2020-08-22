package om.xuecheng.service;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.web.multipart.MultipartFile;

public interface IFileSystemService {
    UploadFileResult upload(MultipartFile file,
                            String filetag,
                            String businesskey,
                            String metadata);
}
