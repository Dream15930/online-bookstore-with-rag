package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.entity.*;
import com.bookstore.mapper.*;
import com.bookstore.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;

    public ReportServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                             BookMapper bookMapper, CategoryMapper categoryMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.bookMapper = bookMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Map<String, Object>> salesTrend(String startDate, String endDate, String period) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDate.parse(startDate, fmt).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, fmt).plusDays(1).atStartOfDay();

        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.between(Order::getCreatedAt, start, end)
          .ne(Order::getStatus, "pending");
        List<Order> orders = orderMapper.selectList(qw);

        Map<String, Map<String, Object>> groups = new LinkedHashMap<>();
        DateTimeFormatter keyFmt = DateTimeFormatter.ofPattern(
            "monthly".equals(period) ? "yyyy-MM" : "quarterly".equals(period) ? "yyyy-'Q'Q" : "yyyy"
        );

        for (Order o : orders) {
            String key = o.getCreatedAt().format(keyFmt);
            groups.putIfAbsent(key, new HashMap<>(Map.of("period", key, "orderCount", 0L, "totalSales", BigDecimal.ZERO)));
            Map<String, Object> g = groups.get(key);
            g.put("orderCount", (Long) g.get("orderCount") + 1);
            g.put("totalSales", ((BigDecimal) g.get("totalSales")).add(o.getTotalAmount()));
        }
        return new ArrayList<>(groups.values());
    }

    @Override
    public List<Map<String, Object>> bookRanking(String startDate, String endDate, Long categoryId, String bookName) {
        List<OrderItem> items = getOrderItemsInRange(startDate, endDate);

        Map<Long, Book> bookMap = bookMapper.selectList(null).stream()
            .filter(b -> (categoryId == null || b.getCategoryId().equals(categoryId))
                      && (bookName == null || b.getTitle().contains(bookName)))
            .collect(Collectors.toMap(Book::getId, b -> b));

        Map<Long, Map<String, Object>> groups = new LinkedHashMap<>();
        for (OrderItem oi : items) {
            Book book = bookMap.get(oi.getBookId());
            if (book == null) continue;
            groups.putIfAbsent(book.getId(), new HashMap<>(Map.of(
                "bookId", book.getId(), "title", book.getTitle(), "author", book.getAuthor(),
                "categoryId", book.getCategoryId(), "salesCount", 0L, "totalRevenue", BigDecimal.ZERO
            )));
            Map<String, Object> g = groups.get(book.getId());
            g.put("salesCount", (Long) g.get("salesCount") + oi.getQuantity());
            g.put("totalRevenue", ((BigDecimal) g.get("totalRevenue")).add(oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity()))));
        }

        return groups.values().stream()
            .sorted((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")))
            .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> categoryStats(String startDate, String endDate) {
        List<OrderItem> items = getOrderItemsInRange(startDate, endDate);
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
            .collect(Collectors.toMap(Category::getId, Category::getName));

        Map<Long, Map<String, Object>> groups = new LinkedHashMap<>();
        for (OrderItem oi : items) {
            Book book = bookMapper.selectById(oi.getBookId());
            if (book == null || book.getCategoryId() == null) continue;
            Long catId = book.getCategoryId();
            groups.putIfAbsent(catId, new HashMap<>(Map.of(
                "categoryId", catId, "categoryName", catMap.getOrDefault(catId, "未分类"),
                "salesCount", 0L, "totalRevenue", BigDecimal.ZERO
            )));
            Map<String, Object> g = groups.get(catId);
            g.put("salesCount", (Long) g.get("salesCount") + oi.getQuantity());
            g.put("totalRevenue", ((BigDecimal) g.get("totalRevenue")).add(oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity()))));
        }
        return new ArrayList<>(groups.values());
    }

    @Override
    public Map<String, Object> summary(String startDate, String endDate) {
        List<OrderItem> items = getOrderItemsInRange(startDate, endDate);
        long orderCount = items.stream().map(OrderItem::getOrderId).distinct().count();
        long bookCount = items.stream().mapToLong(OrderItem::getQuantity).sum();
        BigDecimal revenue = items.stream()
            .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("orderCount", orderCount, "bookCount", bookCount, "totalRevenue", revenue);
    }

    private List<OrderItem> getOrderItemsInRange(String startDate, String endDate) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDate.parse(startDate, fmt).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, fmt).plusDays(1).atStartOfDay();

        LambdaQueryWrapper<Order> oqw = new LambdaQueryWrapper<>();
        oqw.between(Order::getCreatedAt, start, end)
           .ne(Order::getStatus, "pending");
        List<Long> orderIds = orderMapper.selectList(oqw).stream().map(Order::getId).toList();
        if (orderIds.isEmpty()) return List.of();

        LambdaQueryWrapper<OrderItem> iqw = new LambdaQueryWrapper<>();
        iqw.in(OrderItem::getOrderId, orderIds);
        return orderItemMapper.selectList(iqw);
    }
}
