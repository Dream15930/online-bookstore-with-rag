package com.bookstore.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartVO {
    private Long id;
    private Long bookId;
    private String title;
    private String author;
    private String coverUrl;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
}
