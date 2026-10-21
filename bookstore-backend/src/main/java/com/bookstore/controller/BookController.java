package com.bookstore.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.dto.BookVO;
import com.bookstore.dto.ReviewVO;
import com.bookstore.entity.*;
import com.bookstore.mapper.*;
import com.bookstore.service.BookService;
import com.bookstore.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;
    private final ReviewMapper reviewMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    public BookController(BookService bookService, ReviewMapper reviewMapper,
                          OrderItemMapper orderItemMapper, OrderMapper orderMapper,
                          BookMapper bookMapper, UserMapper userMapper) {
        this.bookService = bookService;
        this.reviewMapper = reviewMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderMapper = orderMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/books")
    public Result<Page<BookVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "12") Integer size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long categoryId) {
        size = Math.min(size, 100);
        return Result.ok(bookService.page(page, size, keyword, categoryId));
    }

    @GetMapping("/books/{id}")
    public Result<BookVO> detail(@PathVariable Long id) {
        return Result.ok(bookService.detail(id));
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(bookService.categories());
    }

    @GetMapping("/books/{id}/reviews")
    public Result<List<ReviewVO>> bookReviews(@PathVariable Long id) {
        // 找到该图书的所有已完成订单的 order_items
        LambdaQueryWrapper<OrderItem> oiqw = new LambdaQueryWrapper<>();
        oiqw.eq(OrderItem::getBookId, id);
        List<Long> orderIds = orderItemMapper.selectList(oiqw).stream()
            .map(OrderItem::getOrderId).distinct().collect(Collectors.toList());
        if (orderIds.isEmpty()) return Result.ok(List.of());

        LambdaQueryWrapper<Review> rqw = new LambdaQueryWrapper<>();
        rqw.in(Review::getOrderId, orderIds).orderByDesc(Review::getCreatedAt);
        List<Review> reviews = reviewMapper.selectList(rqw);

        Map<Long, String> userMap = new HashMap<>();
        List<ReviewVO> vos = reviews.stream().map(r -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(r.getId()); vo.setUserId(r.getUserId());
            vo.setOrderId(r.getOrderId()); vo.setContent(r.getContent());
            vo.setRating(r.getRating()); vo.setAdminReply(r.getAdminReply());
            vo.setRepliedAt(r.getRepliedAt()); vo.setCreatedAt(r.getCreatedAt());
            String uname = userMap.computeIfAbsent(r.getUserId(), uid -> {
                User u = userMapper.selectById(uid);
                return u != null ? u.getUsername() : "匿名";
            });
            vo.setUsername(uname);
            return vo;
        }).collect(Collectors.toList());
        return Result.ok(vos);
    }
}
