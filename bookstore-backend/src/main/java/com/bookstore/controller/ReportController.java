package com.bookstore.controller;

import com.bookstore.service.ReportService;
import com.bookstore.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(@RequestParam String startDate,
                                                @RequestParam String endDate) {
        return Result.ok(reportService.summary(startDate, endDate));
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam String startDate,
                                                    @RequestParam String endDate,
                                                    @RequestParam(defaultValue = "monthly") String period) {
        return Result.ok(reportService.salesTrend(startDate, endDate, period));
    }

    @GetMapping("/books")
    public Result<List<Map<String, Object>>> books(@RequestParam String startDate,
                                                    @RequestParam String endDate,
                                                    @RequestParam(required = false) Long categoryId,
                                                    @RequestParam(required = false) String bookName) {
        return Result.ok(reportService.bookRanking(startDate, endDate, categoryId, bookName));
    }

    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories(@RequestParam String startDate,
                                                          @RequestParam String endDate) {
        return Result.ok(reportService.categoryStats(startDate, endDate));
    }
}
