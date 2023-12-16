package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@Slf4j
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {

    final AliOssUtil aliOssUtil;

    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 文件url
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);

        try {
            int index = file.getOriginalFilename().lastIndexOf(".");
            String suffix = file.getOriginalFilename().substring(index);
            String objectName = UUID.randomUUID().toString() + suffix;
            String url = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
