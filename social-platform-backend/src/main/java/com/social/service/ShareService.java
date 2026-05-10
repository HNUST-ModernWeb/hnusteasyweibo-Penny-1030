package com.social.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.entity.Share;
import com.social.entity.ShareImage;
import com.social.entity.User;
import com.social.mapper.ShareMapper;
import com.social.mapper.ShareImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ShareService {
    private final ShareMapper shareMapper;
    private final ShareImageMapper shareImageMapper;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public Share createShare(Long userId, String content, String visibility, List<String> images) {
        System.out.println("=== createShare 开始 ===");
        System.out.println("userId: " + userId);
        System.out.println("content: " + content);

        Share share = new Share();
        share.setUserId(userId);
        share.setContent(content);
        share.setVisibility(visibility);
        share.setLikeCount(0);
        share.setCreateTime(LocalDateTime.now());
        share.setUpdateTime(LocalDateTime.now());

        int result = shareMapper.insert(share);
        System.out.println("插入结果: " + result + ", 生成ID: " + share.getId());

        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                ShareImage image = new ShareImage();
                image.setShareId(share.getId());
                image.setImageUrl(images.get(i));
                image.setSortOrder(i);
                shareImageMapper.insert(image);
            }
        }

        return getShareWithDetails(share.getId());
    }

    public Page<Share> getShares(int page, int size, Long currentUserId) {
        System.out.println("=== getShares 开始 ===");
        System.out.println("page: " + page + ", size: " + size);
        System.out.println("currentUserId: " + currentUserId);

        try {
            Page<Share> pageParam = new Page<>(page, size);
            QueryWrapper<Share> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("create_time");

            System.out.println("执行数据库查询...");
            Page<Share> sharePage = shareMapper.selectPage(pageParam, wrapper);
            System.out.println("查询成功，记录数: " + sharePage.getRecords().size());

            // 为每个分享设置用户信息
            for (Share share : sharePage.getRecords()) {
                try {
                    User user = userService.getById(share.getUserId());
                    share.setUser(user);
                    share.setLiked(false);
                } catch (Exception e) {
                    System.out.println("设置用户信息失败: " + e.getMessage());
                }
            }

            return sharePage;
        } catch (Exception e) {
            System.out.println("getShares 错误: " + e.getMessage());
            e.printStackTrace();
            // 返回空页面而不是抛出异常
            return new Page<>(page, size);
        }
    }

    public Share getShareWithDetails(Long shareId) {
        try {
            Share share = shareMapper.selectById(shareId);
            if (share == null) return null;

            User user = userService.getById(share.getUserId());
            share.setUser(user);

            QueryWrapper<ShareImage> imageWrapper = new QueryWrapper<>();
            imageWrapper.eq("share_id", shareId).orderByAsc("sort_order");
            List<ShareImage> images = shareImageMapper.selectList(imageWrapper);
            share.setImages(images);

            return share;
        } catch (Exception e) {
            System.out.println("getShareWithDetails 错误: " + e.getMessage());
            return null;
        }
    }

    @Transactional
    public boolean deleteShare(Long shareId, Long userId, String role) {
        Share share = shareMapper.selectById(shareId);
        if (share == null) return false;

        if (share.getUserId().equals(userId) || "ROLE_ADMIN".equals(role)) {
            QueryWrapper<ShareImage> imageWrapper = new QueryWrapper<>();
            imageWrapper.eq("share_id", shareId);
            shareImageMapper.delete(imageWrapper);
            shareMapper.deleteById(shareId);
            return true;
        }
        return false;
    }

    @Transactional
    public void toggleLike(Long shareId, Long userId) {
        String likeKey = "share:" + shareId + ":like:" + userId;

        Boolean hasLiked = redisTemplate.hasKey(likeKey);

        System.out.println("=== toggleLike ===");
        System.out.println("shareId: " + shareId + ", userId: " + userId);
        System.out.println("hasLiked: " + hasLiked);

        if (Boolean.TRUE.equals(hasLiked)) {
            redisTemplate.delete(likeKey);
            shareMapper.decrementLikeCount(shareId);
            System.out.println("取消点赞");
        } else {
            redisTemplate.opsForValue().set(likeKey, "1", 7, TimeUnit.DAYS);
            shareMapper.incrementLikeCount(shareId);
            System.out.println("添加点赞");
        }
    }
}