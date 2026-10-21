package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.dto.OrderVO;
import com.bookstore.dto.ReviewRequest;
import com.bookstore.entity.*;
import com.bookstore.exception.BizException;
import com.bookstore.exception.ErrorCode;
import com.bookstore.mapper.*;
import com.bookstore.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final ReviewMapper reviewMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            CartItemMapper cartItemMapper, BookMapper bookMapper,
                            UserMapper userMapper, ReviewMapper reviewMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
        this.reviewMapper = reviewMapper;
    }

    @Override
    @Transactional
    public void place(Long userId, String address) {
        LambdaQueryWrapper<CartItem> qw = new LambdaQueryWrapper<>();
        qw.eq(CartItem::getUserId, userId);
        List<CartItem> cartItems = cartItemMapper.selectList(qw);
        if (cartItems.isEmpty()) {
            throw ErrorCode.CART_EMPTY.ex();
        }

        // 用 SELECT ... FOR UPDATE 悲观锁一次性校验库存并扣减
        // 缓存查询结果，避免后续重复查询
        BigDecimal total = BigDecimal.ZERO;
        java.util.Map<Long, Book> bookCache = new java.util.HashMap<>();
        for (CartItem item : cartItems) {
            Book book = bookMapper.selectByIdForUpdate(item.getBookId());
            if (book == null || book.getStatus() == 0) {
                throw new BizException(ErrorCode.BOOK_OFF_SHELF.getCode(),
                    "图书「" + (book != null ? book.getTitle() : "未知") + "」已下架");
            }
            if (book.getStock() < item.getQuantity()) {
                throw new BizException(ErrorCode.STOCK_INSUFFICIENT.getCode(),
                    "图书「" + book.getTitle() + "」库存不足");
            }
            // 在同一把锁内直接扣库存
            book.setStock(book.getStock() - item.getQuantity());
            bookMapper.updateById(book);
            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            bookCache.put(item.getBookId(), book);
        }

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setAddress(address);
        order.setTotalAmount(total);
        order.setStatus("paid");
        orderMapper.insert(order);
        log.info("新订单创建: orderNo={}, userId={}, amount={}", order.getOrderNo(), userId, order.getTotalAmount());

        // 创建订单明细（复用缓存，不再查库）
        for (CartItem item : cartItems) {
            Book book = bookCache.get(item.getBookId());
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setBookId(item.getBookId());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(book.getPrice());
            orderItemMapper.insert(oi);
        }

        // 清空购物车
        cartItemMapper.delete(qw);
    }

    @Override
    public Page<OrderVO> pageByUser(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.eq(Order::getUserId, userId).orderByDesc(Order::getCreatedAt);
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), qw);
        return convertToVOPage(orderPage);
    }

    @Override
    public Page<OrderVO> pageAll(Integer page, Integer size) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(Order::getCreatedAt);
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), qw);
        return convertToVOPage(orderPage);
    }

    @Override
    public OrderVO detail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) return null;
        return convertToVO(order);
    }

    @Override
    public void updateStatus(Long orderId, String status) {
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            order.setStatus(status);
            orderMapper.updateById(order);
        }
    }

    @Override
    public void completeOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw ErrorCode.ACCESS_DENIED.ex();
        }
        if (!"shipped".equals(order.getStatus())) {
            throw ErrorCode.ORDER_STATUS_ERROR.ex();
        }
        order.setStatus("completed");
        orderMapper.updateById(order);
    }

    @Override
    public void review(Long orderId, Long userId, ReviewRequest req) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw ErrorCode.ACCESS_DENIED.ex();
        }
        if (!"completed".equals(order.getStatus())) {
            throw ErrorCode.ORDER_STATUS_ERROR.ex();
        }
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getOrderId, orderId);
        if (reviewMapper.selectCount(qw) > 0) {
            throw ErrorCode.ALREADY_REVIEWED.ex();
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setOrderId(orderId);
        review.setContent(req.getContent());
        review.setRating(req.getRating());
        reviewMapper.insert(review);
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return date + uuid;
    }

    private Page<OrderVO> convertToVOPage(Page<Order> orderPage) {
        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setUserId(order.getUserId());
        vo.setOrderNo(order.getOrderNo());
        vo.setAddress(order.getAddress());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());

        User user = userMapper.selectById(order.getUserId());
        if (user != null) vo.setUsername(user.getUsername());

        LambdaQueryWrapper<OrderItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemQw);
        vo.setItems(items.stream().map(i -> {
            OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
            itemVO.setId(i.getId());
            itemVO.setBookId(i.getBookId());
            itemVO.setQuantity(i.getQuantity());
            itemVO.setPrice(i.getPrice());
            Book book = bookMapper.selectById(i.getBookId());
            if (book != null) {
                itemVO.setTitle(book.getTitle());
                itemVO.setCoverUrl(book.getCoverUrl());
            }
            return itemVO;
        }).collect(Collectors.toList()));

        LambdaQueryWrapper<Review> reviewQw = new LambdaQueryWrapper<>();
        reviewQw.eq(Review::getOrderId, order.getId());
        Review review = reviewMapper.selectOne(reviewQw);
        if (review != null) {
            OrderVO.ReviewVO reviewVO = new OrderVO.ReviewVO();
            reviewVO.setId(review.getId());
            reviewVO.setContent(review.getContent());
            reviewVO.setRating(review.getRating());
            reviewVO.setCreatedAt(review.getCreatedAt());
            vo.setReview(reviewVO);
        }

        return vo;
    }
}
