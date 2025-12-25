package com.bookshop.bookshop.service;

import com.bookshop.bookshop.pojo.Book;
import java.util.List;
import java.util.Set;

public interface BookService {
    List<Book> getList();              // 获取主页列表
    List<Book> search(String keyword); // 搜索
    Book getBookById(Integer id);
    Set<String> getHotSearchKeywords();
}