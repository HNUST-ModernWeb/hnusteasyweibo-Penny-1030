package com.social.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("share_image")
public class ShareImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shareId;

    private String imageUrl;

    private Integer sortOrder;
}