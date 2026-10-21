package com.bookstore.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Long userId;
    private Long orderId;
    private String username;
    private String bookTitle;
    private String content;
    private Integer rating;
    private String adminReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}
