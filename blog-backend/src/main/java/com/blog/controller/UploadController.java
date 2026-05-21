package com.blog.controller;

import com.blog.common.Result;
import com.blog.common.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Api(tags = "文件上传模块")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @ApiOperation("上传图片")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                    HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserIdOrNull(request);
        if (userId == null) {
            return Result.error(Result.UNAUTHORIZED, "请先登录");
        }

        if (file.isEmpty()) {
            return Result.error(Result.BAD_REQUEST, "文件为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error(Result.BAD_REQUEST, "文件大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(Result.BAD_REQUEST, "只支持上传图片文件");
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            // 扩展名白名单
            java.util.Set<String> allowedExtensions = java.util.Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg");
            if (!allowedExtensions.contains(ext)) {
                return Result.error(Result.BAD_REQUEST, "不支持的文件类型: " + ext);
            }
            String fileName = UUID.randomUUID().toString() + ext;

            File dest = new File(uploadDir, fileName);
            file.transferTo(dest);

            String url = "/uploads/" + fileName;
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }
}
