package com.social.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.dto.ShareDTO;
import com.social.entity.Share;
import com.social.service.ShareService;
import com.social.utils.Result;
import com.social.utils.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ShareController {
    private final ShareService shareService;

    @PostMapping
    public Result<Share> createShare(@Valid @RequestBody ShareDTO shareDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            userId = 1L; // 临时使用默认用户ID
        }
        Share share = shareService.createShare(userId, shareDTO.getContent(),
                shareDTO.getVisibility(), shareDTO.getImages());
        return Result.success("发布成功", share);
    }

    @GetMapping
    public Result<Page<Share>> getShares(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<Share> shares = shareService.getShares(page, size, currentUserId);
        return Result.success(shares);
    }

    @GetMapping("/{id}")
    public Result<Share> getShareById(@PathVariable Long id) {
        Share share = shareService.getShareWithDetails(id);
        if (share == null) {
            return Result.error(404, "分享不存在");
        }
        return Result.success(share);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteShare(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        if (userId == null) {
            userId = 1L;
            role = "ROLE_ADMIN";
        }
        boolean success = shareService.deleteShare(id, userId, role);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error(403, "无权限删除");
    }

    @PostMapping("/{id}/like")
    public Result<?> toggleLike(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            userId = 1L;
        }
        shareService.toggleLike(id, userId);
        return Result.success("操作成功", null);
    }

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            String uploadPath = System.getProperty("user.dir") + "/uploads/";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File destFile = new File(uploadPath + fileName);
            file.transferTo(destFile);

            return Result.success("/uploads/" + fileName);
        } catch (IOException e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}