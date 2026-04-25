package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.Blog;

public interface BlogService extends IService<Blog> {
    /** 查询热门探店笔记 */
    Result queryHotBlog(Integer current);

    /** 查询笔记详情 */
    Result queryBlogById(Long id);

    /** 发布探店笔记 */
    Result saveBlog(Blog blog);

    /** 点赞/取消点赞 */
    Result likeBlog(Long id);

    /** 点赞排行榜（Top5，ZSet按时间） */
    Result queryBlogLikes(Long id);

    /** 关注用户的动态 Feed 流（推模式，滚动分页） */
    Result queryBlogOfFollow(Long max, Integer offset);
}
