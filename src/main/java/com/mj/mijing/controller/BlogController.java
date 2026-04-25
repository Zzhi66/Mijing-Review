package com.mj.mijing.controller;

import com.mj.mijing.dto.Result;
import com.mj.mijing.service.BlogService;
import org.springframework.web.bind.annotation.*;
import com.mj.mijing.entity.Blog;

import javax.annotation.Resource;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    /** 热门探店笔记 */
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    /** 查询笔记详情 */
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable Long id) {
        return blogService.queryBlogById(id);
    }

    /** 发布笔记 */
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    /** 点赞/取消点赞 */
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable Long id) {
        return blogService.likeBlog(id);
    }

    /** 点赞排行榜 */
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable Long id) {
        return blogService.queryBlogLikes(id);
    }

    /** 关注用户动态 Feed 流（滚动分页） */
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return blogService.queryBlogOfFollow(max, offset);
    }
}
