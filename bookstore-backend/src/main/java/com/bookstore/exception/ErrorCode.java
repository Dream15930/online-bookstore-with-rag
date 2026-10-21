package com.bookstore.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 用户相关 1xxx
    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "用户名或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被停用"),
    NOT_ADMIN(1005, "该账号不是管理员"),
    NOT_SUPER_ADMIN(1006, "无权操作管理员账号"),

    // 图书相关 2xxx
    BOOK_NOT_FOUND(2001, "图书不存在"),
    BOOK_OFF_SHELF(2002, "图书已下架"),
    STOCK_INSUFFICIENT(2003, "库存不足"),

    // 订单相关 3xxx
    CART_EMPTY(3001, "购物车为空"),
    ORDER_NOT_FOUND(3002, "订单不存在"),
    ORDER_STATUS_ERROR(3003, "当前状态不可执行此操作"),
    ALREADY_REVIEWED(3004, "已评价过该订单"),
    ADDRESS_EMPTY(3005, "收货地址不能为空"),

    // 权限 4xxx
    UNAUTHORIZED(4001, "请先登录"),
    ACCESS_DENIED(4002, "无权访问"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public BizException ex() { return new BizException(this); }
}
