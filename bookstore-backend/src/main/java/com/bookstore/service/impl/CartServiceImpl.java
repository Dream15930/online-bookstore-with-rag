package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.dto.CartVO;
import com.bookstore.entity.Book;
import com.bookstore.entity.CartItem;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.CartItemMapper;
import com.bookstore.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final BookMapper bookMapper;

    public CartServiceImpl(CartItemMapper cartItemMapper, BookMapper bookMapper) {
        this.cartItemMapper = cartItemMapper;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<CartVO> list(Long userId) {
        LambdaQueryWrapper<CartItem> qw = new LambdaQueryWrapper<>();
        qw.eq(CartItem::getUserId, userId);
        return cartItemMapper.selectList(qw).stream().map(item -> {
            Book book = bookMapper.selectById(item.getBookId());
            CartVO vo = new CartVO();
            vo.setId(item.getId());
            vo.setBookId(item.getBookId());
            vo.setQuantity(item.getQuantity());
            if (book != null) {
                vo.setTitle(book.getTitle());
                vo.setAuthor(book.getAuthor());
                vo.setCoverUrl(book.getCoverUrl());
                vo.setPrice(book.getPrice());
                vo.setStock(book.getStock());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void add(Long userId, Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getStatus() == 0) {
            throw new RuntimeException("图书不存在或已下架");
        }
        LambdaQueryWrapper<CartItem> qw = new LambdaQueryWrapper<>();
        qw.eq(CartItem::getUserId, userId).eq(CartItem::getBookId, bookId);
        CartItem exist = cartItemMapper.selectOne(qw);
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + 1);
            cartItemMapper.updateById(exist);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setBookId(bookId);
            item.setQuantity(1);
            cartItemMapper.insert(item);
        }
    }

    @Override
    public void updateQuantity(Long id, Long userId, Integer quantity) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该购物车项");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    @Override
    public void delete(Long id, Long userId) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该购物车项");
        }
        cartItemMapper.deleteById(id);
    }

    @Override
    public void clearByUserId(Long userId) {
        LambdaQueryWrapper<CartItem> qw = new LambdaQueryWrapper<>();
        qw.eq(CartItem::getUserId, userId);
        cartItemMapper.delete(qw);
    }
}
