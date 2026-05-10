package com.social.controller;

import com.social.dto.CommentDTO;
import com.social.entity.Comment;
import com.social.service.CommentService;
import com.social.utils.Result;
import com.social.utils.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shares/{shareId}/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public Result<Comment> createComment(@PathVariable Long shareId,
                                         @Valid @RequestBody CommentDTO commentDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Comment comment = commentService.createComment(shareId, userId,
                commentDTO.getContent(),
                commentDTO.getParentId());
        return Result.success("评论成功", comment);
    }

    @GetMapping
    public Result<List<Comment>> getComments(@PathVariable Long shareId) {
        List<Comment> comments = commentService.getCommentsByShareId(shareId);
        return Result.success(comments);
    }

    @DeleteMapping("/{commentId}")
    public Result<?> deleteComment(@PathVariable Long shareId, @PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean success = commentService.deleteComment(commentId, userId, role);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error(403, "无权限删除");
    }
}