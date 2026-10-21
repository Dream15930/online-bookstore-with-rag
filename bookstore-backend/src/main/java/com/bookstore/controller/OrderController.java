package com.bookstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.dto.OrderRequest;
import com.bookstore.dto.OrderVO;
import com.bookstore.dto.ReviewRequest;
import com.bookstore.service.OrderService;
import com.bookstore.util.Result;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<Void> place(Authentication auth, @Valid @RequestBody OrderRequest req) {
        Long userId = Long.valueOf(auth.getName());
        orderService.place(userId, req.getAddress());
        return Result.ok();
    }

    @GetMapping
    public Result<Page<OrderVO>> myOrders(Authentication auth,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        size = Math.min(size, 100);
        Long userId = Long.valueOf(auth.getName());
        return Result.ok(orderService.pageByUser(userId, page, size));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(Authentication auth, @PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        orderService.completeOrder(id, userId);
        return Result.ok();
    }

    @PostMapping("/{id}/review")
    public Result<Void> review(Authentication auth, @PathVariable Long id,
                                @Valid @RequestBody ReviewRequest req) {
        Long userId = Long.valueOf(auth.getName());
        orderService.review(id, userId, req);
        return Result.ok();
    }
}
