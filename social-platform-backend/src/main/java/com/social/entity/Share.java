package com.social.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("share")
public class Share {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String content;

    private String visibility;

    private Integer likeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private List<ShareImage> images;

    @TableField(exist = false)
    private Boolean liked;
}