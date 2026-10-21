package com.bookstore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank(message = "评价内容不能为空")
    private String content;
    @Min(1) @Max(5)
    private Integer rating;
}
