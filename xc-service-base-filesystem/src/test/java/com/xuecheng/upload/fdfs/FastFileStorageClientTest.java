package com.xuecheng.upload.fdfs;


import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.fdfs.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import om.xuecheng.FileSystemApplication;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * FastFileStorageClient客户端
 *
 * @author tobato
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = FileSystemApplication.class)
public class FastFileStorageClientTest {

    @Autowired
    protected FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;

    /**
     * 日志
     */
    protected static Logger LOGGER = LoggerFactory.getLogger(FastFileStorageClientTest.class);

    /**
     * 上传文件，并且设置MetaData
     */
    @Test
    public void testUploadFileAndMetaData() throws FileNotFoundException {

        LOGGER.debug("##上传文件..##");
        File file = new File("E:\\my_java\\uploads");
        // Metadata
        Set<MetaData> metaDataSet = createMetaData();
        // 上传文件和Metadata
        StorePath path = storageClient.uploadFile(new FileInputStream(file), file.length(), "jpeg",
                metaDataSet);
        assertNotNull(path);
        LOGGER.debug("上传文件路径{}", path);

        // 验证获取MetaData
        LOGGER.debug("##获取Metadata##");
        Set<MetaData> fetchMetaData = storageClient.getMetadata(path.getGroup(), path.getPath());
        assertEquals(fetchMetaData, metaDataSet);

        LOGGER.debug("##删除文件..##");
        storageClient.deleteFile(path.getGroup(), path.getPath());
    }

    /**
     * 不带MetaData也应该能上传成功
     */
    @Test
    public void testUploadFileWithoutMetaData() throws FileNotFoundException {

        LOGGER.debug("##上传文件..##");
        //RandomTextFile file = new RandomTextFile();
        File file = new File("E:\\my_java\\uploads\\testfdfs.jpg");
        // 上传文件和Metadata
        StorePath path = storageClient.uploadFile(new FileInputStream(file), file.length(), "jpg",
                null);
        assertNotNull(path);

        System.out.println(path.getFullPath());

        LOGGER.debug("##删除文件..##");
        storageClient.deleteFile(path.getFullPath());
    }

    /**
     * 上传图片，并且生成缩略图
     * <pre>
     *     缩略图为上传文件名+缩略图后缀 _150x150,如 xxx.jpg,缩略图为 xxx_150x150.jpg
     *
     * 实际样例如下
     *
     * http://localhost:8098/M00/00/17/rBEAAl33pQaAWNQNAAHYvQQn-YE374.jpg
     * http://localhost:8098/M00/00/17/rBEAAl33pQaAWNQNAAHYvQQn-YE374_150x150.jpg
     * </pre>
     */
    /*@Test
    public void testUploadImageAndCrtThumbImage() {
        LOGGER.debug("##上传文件..##");
        Set<MetaData> metaDataSet = createMetaData();
        StorePath path = uploadImageAndCrtThumbImage(TestConstants.PERFORM_FILE_PATH, metaDataSet);
        LOGGER.debug("上传文件路径{}", path);

        // 验证获取MetaData
        LOGGER.debug("##获取Metadata##");
        Set<MetaData> fetchMetaData = storageClient.getMetadata(path.getGroup(), path.getPath());
        assertEquals(fetchMetaData, metaDataSet);

        // 验证获取从文件
        LOGGER.debug("##获取Metadata##");
        // 这里需要一个获取从文件名的能力，所以从文件名配置以后就最好不要改了
        String slavePath = thumbImageConfig.getThumbImagePath(path.getPath());
        // 或者由客户端再记录一下从文件的前缀
        FileInfo slaveFile = storageClient.queryFileInfo(path.getGroup(), slavePath);
        assertNotNull(slaveFile);
        LOGGER.debug("##获取到从文件##{}", slaveFile);

    }*/

    /**
     * 上传文件
     *
     * @param filePath
     * @return
     */
    private StorePath uploadImageAndCrtThumbImage(String filePath, Set<MetaData> metaDataSet) {
        InputStream in = null;
        File file = new File("E:\\my_java\\uploads");
        String fileExtName = FilenameUtils.getExtension(file.getName());
        long fileSize = file.length();
        try {
            //in = TestUtils.getFileInputStream(filePath);
            in = new FileInputStream(file);
            return storageClient.uploadImageAndCrtThumbImage(in, fileSize, fileExtName, metaDataSet);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    private Set<MetaData> createMetaData() {
        Set<MetaData> metaDataSet = new HashSet<>();
        metaDataSet.add(new MetaData("Author", "tobato"));
        metaDataSet.add(new MetaData("CreateDate", "2016-01-05"));
        return metaDataSet;
    }

}
