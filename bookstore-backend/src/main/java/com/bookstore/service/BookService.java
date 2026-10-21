package com.bookstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.dto.BookVO;
import com.bookstore.entity.Book;
import com.bookstore.entity.Category;

import java.util.List;

public interface BookService {
    Page<BookVO> page(Integer page, Integer size, String keyword, Long categoryId);
    BookVO detail(Long id);
    Page<BookVO> adminPage(Integer page, Integer size);
    void save(Book book);
    void update(Book book);
    void toggleStatus(Long id);
    List<Category> categories();
}
