package com.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.social.entity.Share;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShareMapper extends BaseMapper<Share> {

    @Update("UPDATE share SET like_count = like_count + 1 WHERE id = #{shareId}")
    void incrementLikeCount(Long shareId);

    @Update("UPDATE share SET like_count = like_count - 1 WHERE id = #{shareId}")
    void decrementLikeCount(Long shareId);
}