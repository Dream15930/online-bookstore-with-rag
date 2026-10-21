package com.bookstore.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private Long userId;
    private String username;
    private String orderNo;
    private String address;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;
    private ReviewVO review;

    @Data
    public static class OrderItemVO {
        private Long id;
        private Long bookId;
        private String title;
        private String coverUrl;
        private Integer quantity;
        private BigDecimal price;
    }

    @Data
    public static class ReviewVO {
        private Long id;
        private String content;
        private Integer rating;
        private LocalDateTime createdAt;
    }
}
