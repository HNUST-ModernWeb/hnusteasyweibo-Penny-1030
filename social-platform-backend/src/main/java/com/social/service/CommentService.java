package com.social.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.social.entity.Comment;
import com.social.entity.User;
import com.social.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentMapper commentMapper;
    private final UserService userService;

    @Transactional
    public Comment createComment(Long shareId, Long userId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setShareId(shareId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        return getCommentWithDetails(comment.getId());
    }

    public List<Comment> getCommentsByShareId(Long shareId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("share_id", shareId).orderByAsc("create_time");

        List<Comment> allComments = commentMapper.selectList(wrapper);

        allComments.forEach(comment -> {
            User user = userService.getById(comment.getUserId());
            comment.setUser(user);
        });

        Map<Long, List<Comment>> commentsByParent = allComments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        List<Comment> topComments = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        topComments.forEach(comment -> {
            comment.setReplies(commentsByParent.getOrDefault(comment.getId(), List.of()));
        });

        return topComments;
    }

    public Comment getCommentWithDetails(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            User user = userService.getById(comment.getUserId());
            comment.setUser(user);
        }
        return comment;
    }

    @Transactional
    public boolean deleteComment(Long commentId, Long userId, String role) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) return false;

        if (comment.getUserId().equals(userId) || "ROLE_ADMIN".equals(role)) {
            commentMapper.deleteById(commentId);
            return true;
        }
        return false;
    }
}