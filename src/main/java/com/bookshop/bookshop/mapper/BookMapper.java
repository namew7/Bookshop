package com.bookshop.bookshop.mapper;

import com.bookshop.bookshop.pojo.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BookMapper {

    /**
     * 根据ID查询单本书（用于详情页）
     */
    @Select("select * from books where id = #{id}")
    Book findById(Integer id);

    /**
     * 搜索书籍（用于搜索页）
     * 逻辑：如果 keyword 为空，查所有；如果不为空，按标题或作者模糊查询
     * 注意：MySQL 的 like 默认就是不区分大小写的，所以不用特意加 lower()
     */
    @Select("select * from books where title like concat('%', #{keyword}, '%') " +
            "or author like concat('%', #{keyword}, '%') " +
            "or isbn like concat('%', #{keyword}, '%')")
    List<Book> search(String keyword);

    /**
     * ✅ 新增：只查前 10 本新书（用于首页）
     */
    @Select("select * from books order by id desc limit 10")
    List<Book> findNewBooks();

    /**
     * 扣减库存
     * 返回影响行数，如果返回0说明库存不足(where quantity >= #{count} 条件不满足)
     */
    @Update(
            "update books set quantity = quantity - #{count} " +
                    "where id = #{id} and quantity >= #{count}"
    )
    int reduceStock(Integer id, Integer count);

    //可选优化管理员新增，还未实现
    void update(Book book);
}