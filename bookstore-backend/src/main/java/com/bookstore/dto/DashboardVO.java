package com.bookstore.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardVO {
    private Long bookCount;
    private Long orderCount;
    private BigDecimal todaySales;
}
