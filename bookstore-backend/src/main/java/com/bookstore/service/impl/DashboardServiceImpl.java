package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.dto.DashboardVO;
import com.bookstore.entity.Book;
import com.bookstore.entity.Order;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final BookMapper bookMapper;
    private final OrderMapper orderMapper;

    public DashboardServiceImpl(BookMapper bookMapper, OrderMapper orderMapper) {
        this.bookMapper = bookMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public DashboardVO getStats() {
        DashboardVO vo = new DashboardVO();
        vo.setBookCount(bookMapper.selectCount(null));

        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.eq(Order::getStatus, "paid").or().eq(Order::getStatus, "shipped").or().eq(Order::getStatus, "completed");
        vo.setOrderCount(orderMapper.selectCount(qw));

        LambdaQueryWrapper<Order> todayQw = new LambdaQueryWrapper<>();
        todayQw.ge(Order::getCreatedAt, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        BigDecimal todaySales = orderMapper.selectList(todayQw).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodaySales(todaySales);

        return vo;
    }
}
