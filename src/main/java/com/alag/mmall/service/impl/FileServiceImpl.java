package com.alag.mmall.service.impl;

import com.alag.mmall.common.FTPUtil;
import com.alag.mmall.service.FileService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Override
    public String upload(MultipartFile file, String path) {
        String originalFileName = file.getOriginalFilename();
        String fileExtensionName = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        log.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",originalFileName,path,uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);
        try {
            file.transferTo(targetFile);
            log.info("上传文件成功");
            if (FTPUtil.uploadFile(Lists.newArrayList(targetFile))) {
                targetFile.delete();
            }
            log.info("文件已上传至FTP服务器");

        } catch (IOException e) {
            e.printStackTrace();
            log.info("上传文件失败");
            return null;
        }
        return targetFile.getName();
    }
}
