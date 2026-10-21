package com.bookstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.dto.BookVO;
import com.bookstore.dto.DashboardVO;
import com.bookstore.dto.OrderVO;
import com.bookstore.dto.ReviewVO;
import com.bookstore.entity.Book;
import com.bookstore.entity.OrderItem;
import com.bookstore.entity.Review;
import com.bookstore.entity.User;
import com.bookstore.mapper.*;
import com.bookstore.service.BookService;
import com.bookstore.service.DashboardService;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import com.bookstore.util.Result;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BookService bookService;
    private final OrderService orderService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final OrderItemMapper orderItemMapper;

    public AdminController(BookService bookService, OrderService orderService,
                           UserService userService, DashboardService dashboardService,
                           ReviewMapper reviewMapper, UserMapper userMapper,
                           BookMapper bookMapper, OrderItemMapper orderItemMapper) {
        this.bookService = bookService;
        this.orderService = orderService;
        this.userService = userService;
        this.dashboardService = dashboardService;
        this.reviewMapper = reviewMapper;
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
        this.orderItemMapper = orderItemMapper;
    }

    private String callerRole(Authentication auth) {
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) auth.getDetails();
        return details != null ? details.get("role") : "member";
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.ok(dashboardService.getStats());
    }

    @GetMapping("/books")
    public Result<Page<BookVO>> books(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        size = Math.min(size, 100);
        return Result.ok(bookService.adminPage(page, size));
    }

    @PostMapping("/books")
    public Result<Void> addBook(@RequestBody Book book) {
        bookService.save(book);
        return Result.ok();
    }

    @PutMapping("/books/{id}")
    public Result<Void> updateBook(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        bookService.update(book);
        return Result.ok();
    }

    @PutMapping("/books/{id}/status")
    public Result<Void> toggleBookStatus(@PathVariable Long id) {
        bookService.toggleStatus(id);
        return Result.ok();
    }

    @GetMapping("/orders")
    public Result<Page<OrderVO>> orders(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        size = Math.min(size, 100);
        return Result.ok(orderService.pageAll(page, size));
    }

    @GetMapping("/orders/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }

    @PutMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @GetMapping("/users")
    public Result<List<User>> users() {
        List<User> users = userService.listAll();
        users.forEach(u -> u.setPassword(null));
        return Result.ok(users);
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, Authentication auth) {
        userService.toggleStatus(id, callerRole(auth));
        return Result.ok();
    }

    @PostMapping("/users/admin")
    public Result<Void> createAdmin(@RequestBody Map<String, String> body, Authentication auth) {
        userService.createAdmin(body.get("username"), body.get("password"), callerRole(auth));
        return Result.ok();
    }

    // 评价管理
    @GetMapping("/reviews")
    public Result<List<ReviewVO>> reviews(@RequestParam(required = false) Long bookId) {
        LambdaQueryWrapper<Review> rqw = new LambdaQueryWrapper<>();
        rqw.orderByDesc(Review::getCreatedAt);
        List<Review> reviews = reviewMapper.selectList(rqw);

        Map<Long, String> userMap = new HashMap<>();
        Map<Long, String> bookMap = new HashMap<>();

        List<ReviewVO> vos = reviews.stream().map(r -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(r.getId()); vo.setUserId(r.getUserId());
            vo.setOrderId(r.getOrderId()); vo.setContent(r.getContent());
            vo.setRating(r.getRating()); vo.setAdminReply(r.getAdminReply());
            vo.setRepliedAt(r.getRepliedAt()); vo.setCreatedAt(r.getCreatedAt());
            vo.setUsername(userMap.computeIfAbsent(r.getUserId(), uid -> {
                User u = userMapper.selectById(uid);
                return u != null ? u.getUsername() : "匿名";
            }));
            // 获取图书名
            LambdaQueryWrapper<OrderItem> oiqw = new LambdaQueryWrapper<>();
            oiqw.eq(OrderItem::getOrderId, r.getOrderId()).last("limit 1");
            OrderItem oi = orderItemMapper.selectOne(oiqw);
            if (oi != null) {
                vo.setBookTitle(bookMap.computeIfAbsent(oi.getBookId(), bid -> {
                    Book b = bookMapper.selectById(bid);
                    return b != null ? b.getTitle() : "未知";
                }));
            }
            return vo;
        }).collect(Collectors.toList());

        // 按图书筛选
        if (bookId != null) {
            vos = vos.stream().filter(v -> {
                LambdaQueryWrapper<OrderItem> oq = new LambdaQueryWrapper<>();
                oq.eq(OrderItem::getOrderId, v.getOrderId()).eq(OrderItem::getBookId, bookId);
                return orderItemMapper.selectCount(oq) > 0;
            }).collect(Collectors.toList());
        }
        return Result.ok(vos);
    }

    @PutMapping("/reviews/{id}/reply")
    public Result<Void> replyReview(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Review r = reviewMapper.selectById(id);
        if (r != null) {
            r.setAdminReply(body.get("reply"));
            r.setRepliedAt(LocalDateTime.now());
            reviewMapper.updateById(r);
        }
        return Result.ok();
    }
}
