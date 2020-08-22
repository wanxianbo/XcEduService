package com.xuecheng.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FileManage {

    @Test
    public void testFileChunk() throws IOException {
        File sourceFile = new File("H:\\integrated_project\\lucene.avi");
        //分块目录
        String chunkPath = "H:\\integrated_project\\chunk\\";
        //分块大小
        long chunkSize = 1024 * 1024;
        //分块数量
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //设置缓存区大小
        byte[] bytes = new byte[1024];
        //创建文件读对象
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //分块
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File file = new File(chunkPath + i);
            //向分块文件中写数据
            RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
            int len = -1;
            while ((len = raf_read.read(bytes)) != -1) {
                raf_write.write(bytes, 0, len);
                if (file.length() >= chunkSize) {
                    break;
                }
            }
            raf_write.close();
        }
        raf_read.close();
    }

    @Test
    public void testFileMerge() throws IOException {
        String chunkFilePath = "H:\\integrated_project\\chunk\\";
        //块文件目录
        File chunkFile = new File(chunkFilePath);
        //合并文件
        File mergeFile = new File("H:\\integrated_project\\lucene_merge.avi");
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        //创建合并文件
        boolean newFile = mergeFile.createNewFile();
        //创建文件写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //指针指向文件顶端
        raf_write.seek(0);
        //缓冲区
        byte[] b = new byte[1024];
        //将文件排序
        File[] files = chunkFile.listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList,(o1, o2) -> {
            if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                return 1;
            }
            return -1;
        });
        //合并文件
        RandomAccessFile raf_read = null;
        for (File file : fileList) {
            raf_read = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_read.read(b)) != -1) {
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }

    @Test
    public void testHD5() throws FileNotFoundException {
        File file = new File("H:\\integrated_project\\lucene.avi");
        FileInputStream inputStream = new FileInputStream(file);
        try {
            String md5Hex = DigestUtils.md5DigestAsHex(inputStream);
            System.out.println(md5Hex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //7b3afee9bcfefb0d2f8c2cbb3ebb4d66
        //c5c75d70f382e6016d2f506d134eee11
    }
}
