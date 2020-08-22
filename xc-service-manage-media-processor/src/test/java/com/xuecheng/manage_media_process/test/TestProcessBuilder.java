package com.xuecheng.manage_media_process.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestProcessBuilder {

    @Test
    public void testProcessBuilder() {
        //创建processBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder();
        //设置要执行的第三方应用程序的命令
        //processBuilder.command("ping", "127.0.0.1");
        processBuilder.command("ipconfig");
        //设置输入流和错误流合并
        processBuilder.redirectErrorStream(true);
        //执行命令
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (process == null) {
            throw new RuntimeException();
        }
        //得到输入流
        InputStream inputStream = process.getInputStream();
        //读取输入流
        //定义字符输入流
        try {
            InputStreamReader streamReader = new InputStreamReader(inputStream, "gbk");
            int len = -1;
            char[] chars = new char[1024];
            while ((len = streamReader.read(chars)) != -1) {
                String s = new String(chars, 0, len);
                System.out.println(s);
            }
            streamReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Test
    public void testRuntimeProcess() throws Exception {
        //得到runtime对象
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("ipconfig");

        //得到输入流
        InputStream inputStream = process.getInputStream();
        //读取输入流
        InputStreamReader streamReader = new InputStreamReader(inputStream, "gbk");
        int len = -1;
        char[] chars = new char[1024];
        while ((len = streamReader.read(chars)) != -1) {
            String s = new String(chars, 0, len);
            System.out.println(s);
        }
        streamReader.close();
        inputStream.close();

    }
}
