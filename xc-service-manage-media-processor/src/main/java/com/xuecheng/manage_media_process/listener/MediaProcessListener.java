package com.xuecheng.manage_media_process.listener;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessListener {

    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpeg_path;

    //上传文件根目录
    @Value("${xc-service-manage-media.video-location}")
    private String serverPath;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",
            containerFactory = "customContainerFactory")
    public void receiveMediaProcessListener(String msg) {
        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");
        //获取MediaFile
        //查询数据库
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return;
        }
        MediaFile mediaFile = optional.get();
        //判断文件类型（目前只有avi）
        String fileType = mediaFile.getFileType();
        if (!StringUtils.equals(fileType, "avi")) {
            mediaFile.setProcessStatus("303004");//处理状态为无需处理
            mediaFileRepository.save(mediaFile);
            return;
        } else {
            mediaFile.setProcessStatus("303001");//处理状态为未处理
            mediaFileRepository.save(mediaFile);
        }
        String filePath = mediaFile.getFilePath();
        String fileName = mediaFile.getFileName();
        String video_path = serverPath + filePath + fileName;
        String mp4_name = mediaId + ".mp4";
        String mp4folder_path = serverPath + filePath;
        // ffmpeg_path, video_path, mp4_name, mp4folder_path
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        //生成MP4
        String result = mp4VideoUtil.generateMp4();
        //生成MP4失败
        if (generateFail(mediaFile, result)) return;
        //生成m3u8
        //ffmpeg_path, video_path, m3u8_name,m3u8folder_path
        String mp4_path = serverPath + filePath + mp4_name;
        String m3u8_name = mediaId + ".m3u8";
        String m3u8folder_path = serverPath + filePath + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, mp4_path, m3u8_name, m3u8folder_path);
        String hlsResult = hlsVideoUtil.generateM3u8();
        //生m3u8失败
        if (generateFail(mediaFile, hlsResult)) return;
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        mediaFile.setProcessStatus("303002");//处理状态为处理成功
        mediaFile.setFileUrl(filePath + "hls/" + m3u8_name);
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        mediaFileRepository.save(mediaFile);

    }

    //操作失败写入处理日志
    private boolean generateFail(MediaFile mediaFile, String result) {
        if (!StringUtils.equals(result, "success")) {
            // 生成mp4失败
            mediaFile.setProcessStatus("303003");//处理状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return true;
        }
        return false;
    }
}

