package com.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /** 带悲观锁查询图书，防止并发超卖 */
    @Select("SELECT * FROM books WHERE id = #{id} FOR UPDATE")
    Book selectByIdForUpdate(@Param("id") Long id);
}
