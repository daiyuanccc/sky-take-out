package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private HttpMessageConverters messageConverters;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传{}", file);
        try {
            //原始文件名
            String name = file.getOriginalFilename();
            //获取文件后缀
            if (name != null) {
                String suffix = name.substring(name.lastIndexOf("."));
                //生成随机文件名
                String objectName = UUID.randomUUID().toString() + suffix;
                //上传文件
                String upload = aliOssUtil.upload(file.getBytes(), objectName);
                return Result.success(upload);
            }
        } catch (IOException e) {
            log.error(MessageConstant.UPLOAD_FAILED);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
