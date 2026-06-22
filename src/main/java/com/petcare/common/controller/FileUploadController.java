package com.petcare.common.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.config.FileUploadConfig;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * File upload controller for user and catalog images.
 * Stores files to local disk under {project-root}/uploads/.
 * Returns the accessible URL path.
 */
@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final Map<String, String> ALLOWED_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    @PostMapping
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "文件大小不能超过10MB");
        }
        String contentType = file.getContentType();
        String extension = contentType == null ? null : ALLOWED_EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "只支持 JPG/PNG/GIF/WebP 格式");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String subDir = "images";

        File dir = new File(FileUploadConfig.getUploadDir() + subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }

        String url = "/uploads/" + subDir + "/" + fileName;
        return ApiResponse.ok(Map.of("url", url));
    }
}
