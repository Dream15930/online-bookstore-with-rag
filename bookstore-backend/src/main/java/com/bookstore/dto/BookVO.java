package com.bookstore.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookVO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private String coverUrl;
    private String description;
    private Integer status;
}
