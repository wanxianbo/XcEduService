package om.xuecheng.controller;

import com.xuecheng.api.filesystem.FileSystemControllerApi;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import om.xuecheng.service.IFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/filesystem")
public class FileSystemController implements FileSystemControllerApi {

    @Autowired
    private IFileSystemService fileSystemService;

    @Override
    @RequestMapping("/upload")
    public UploadFileResult upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("filetag") String filetag,
                                   @RequestParam(value = "businesskey",required = false)String businesskey,
                                   @RequestParam(value = "metadata",required = false)String metadata) {
        return fileSystemService.upload(file, filetag, businesskey, metadata);
    }

}
