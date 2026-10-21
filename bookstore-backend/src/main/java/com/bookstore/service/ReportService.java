package com.bookstore.service;

import java.util.List;
import java.util.Map;

public interface ReportService {
    List<Map<String, Object>> salesTrend(String startDate, String endDate, String period);
    List<Map<String, Object>> bookRanking(String startDate, String endDate, Long categoryId, String bookName);
    List<Map<String, Object>> categoryStats(String startDate, String endDate);
    Map<String, Object> summary(String startDate, String endDate);
}
