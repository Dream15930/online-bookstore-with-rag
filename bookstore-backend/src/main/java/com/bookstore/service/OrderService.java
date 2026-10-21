package com.bookstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.dto.OrderVO;
import com.bookstore.dto.ReviewRequest;

public interface OrderService {
    void place(Long userId, String address);
    Page<OrderVO> pageByUser(Long userId, Integer page, Integer size);
    Page<OrderVO> pageAll(Integer page, Integer size);
    OrderVO detail(Long orderId);
    void updateStatus(Long orderId, String status);
    void completeOrder(Long orderId, Long userId);
    void review(Long orderId, Long userId, ReviewRequest req);
}
