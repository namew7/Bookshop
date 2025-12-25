package com.bookshop.bookshop.mapper;

import com.bookshop.bookshop.pojo.CartItem;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CartMapper {
    // 查询单条记录
    @Select("select * from cart_items where user_id = #{userId} and book_id = #{bookId}")
    CartItem findOne(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    // 新增记录
    @Insert("insert into cart_items(user_id, book_id, count) values(#{userId}, #{bookId}, #{count})")
    void insert(CartItem cartItem);

    // 更新数量
    @Update("update cart_items set count = #{count} where user_id = #{userId} and book_id = #{bookId}")
    void updateCount(@Param("userId") Integer userId, @Param("bookId") Integer bookId, @Param("count") Integer count);

    // 删除记录
    @Delete("delete from cart_items where user_id = #{userId} and book_id = #{bookId}")
    void delete(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    // 🆕 新增：清空某用户的所有购物车数据
    @Delete("delete from cart_items where user_id = #{userId}")
    void deleteAll(@Param("userId") Integer userId);
}