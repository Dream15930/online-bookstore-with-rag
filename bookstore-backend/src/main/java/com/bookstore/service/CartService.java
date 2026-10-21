package com.bookstore.service;

import com.bookstore.dto.CartVO;

import java.util.List;

public interface CartService {
    List<CartVO> list(Long userId);
    void add(Long userId, Long bookId);
    void updateQuantity(Long id, Long userId, Integer quantity);
    void delete(Long id, Long userId);
    void clearByUserId(Long userId);
}
