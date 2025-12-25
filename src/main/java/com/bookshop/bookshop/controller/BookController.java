package com.bookshop.bookshop.controller;

import com.bookshop.bookshop.pojo.Book;
import com.bookshop.bookshop.pojo.Result;
import com.bookshop.bookshop.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*; // 1. 改为 .* 方便导入 PathVariable

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 首页/搜索二合一接口
     * URL: GET /book/search?keyword=java
     * 逻辑:
     * 1. 刚进页面没传 keyword -> 调用 getList() -> 走 Redis 缓存 (显示前10本新书)
     * 2. 用户搜了词 -> 调用 search() -> 走数据库模糊查询
     */
    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String keyword) {
        List<Book> list;
        if (keyword == null || keyword.trim().isEmpty()) {
            list = bookService.getList();
        } else {
            // 这里走数据库搜索
            list = bookService.search(keyword);
        }
        return Result.success(list);
    }

    /**
     * 书籍详情接口 (优化为 RESTful 风格)
     *
     *  URL: GET /book/detail/1
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) { // 使用 @PathVariable
        if (id == null || id <= 0) {
            return Result.error("参数错误");
        }

        // 命中刚才写的 "详情页缓存" (防穿透、防雪崩)
        Book book = bookService.getBookById(id);

        if (book != null) {
            return Result.success(book);
        } else {
            return Result.error("书籍不存在或已下架");
        }
    }
    /**
     * 获取热搜排行榜
     * URL: GET /book/hot-search
     */
    @GetMapping("/hot-search")
    public Result getHotSearch() {
        Set<String> hotWords = bookService.getHotSearchKeywords();
        return Result.success(hotWords);
    }
}