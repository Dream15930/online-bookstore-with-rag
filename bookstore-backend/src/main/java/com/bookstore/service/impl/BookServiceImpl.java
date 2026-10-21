package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.dto.BookVO;
import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.CategoryMapper;
import com.bookstore.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;

    public BookServiceImpl(BookMapper bookMapper, CategoryMapper categoryMapper) {
        this.bookMapper = bookMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Page<BookVO> page(Integer page, Integer size, String keyword, Long categoryId) {
        LambdaQueryWrapper<Book> qw = new LambdaQueryWrapper<>();
        qw.eq(Book::getStatus, 1);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        if (categoryId != null) {
            qw.eq(Book::getCategoryId, categoryId);
        }
        qw.orderByDesc(Book::getCreatedAt);

        Page<Book> bookPage = bookMapper.selectPage(new Page<>(page, size), qw);
        Map<Long, String> categoryMap = categoryMapper.selectList(null)
                .stream().collect(Collectors.toMap(Category::getId, Category::getName));

        Page<BookVO> voPage = new Page<>(page, size, bookPage.getTotal());
        voPage.setRecords(bookPage.getRecords().stream().map(b -> {
            BookVO vo = new BookVO();
            copyProps(b, vo);
            vo.setCategoryName(categoryMap.getOrDefault(b.getCategoryId(), "未分类"));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public BookVO detail(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) return null;
        BookVO vo = new BookVO();
        copyProps(book, vo);
        Category category = categoryMapper.selectById(book.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : "未分类");
        return vo;
    }

    @Override
    public Page<BookVO> adminPage(Integer page, Integer size) {
        LambdaQueryWrapper<Book> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(Book::getCreatedAt);
        Page<Book> bookPage = bookMapper.selectPage(new Page<>(page, size), qw);
        Map<Long, String> categoryMap = categoryMapper.selectList(null)
                .stream().collect(Collectors.toMap(Category::getId, Category::getName));

        Page<BookVO> voPage = new Page<>(page, size, bookPage.getTotal());
        voPage.setRecords(bookPage.getRecords().stream().map(b -> {
            BookVO vo = new BookVO();
            copyProps(b, vo);
            vo.setCategoryName(categoryMap.getOrDefault(b.getCategoryId(), "未分类"));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void save(Book book) {
        book.setStatus(1);
        bookMapper.insert(book);
    }

    @Override
    public void update(Book book) {
        bookMapper.updateById(book);
    }

    @Override
    public void toggleStatus(Long id) {
        Book book = bookMapper.selectById(id);
        if (book != null) {
            book.setStatus(book.getStatus() == 1 ? 0 : 1);
            bookMapper.updateById(book);
        }
    }

    @Override
    public List<Category> categories() {
        return categoryMapper.selectList(null);
    }

    private void copyProps(Book from, BookVO to) {
        to.setId(from.getId());
        to.setTitle(from.getTitle());
        to.setAuthor(from.getAuthor());
        to.setIsbn(from.getIsbn());
        to.setPrice(from.getPrice());
        to.setStock(from.getStock());
        to.setCategoryId(from.getCategoryId());
        to.setCoverUrl(from.getCoverUrl());
        to.setDescription(from.getDescription());
        to.setStatus(from.getStatus());
    }
}
