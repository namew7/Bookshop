package com.bookshop.bookshop.service.impl;
import com.bookshop.bookshop.mapper.BookMapper;
import com.bookshop.bookshop.pojo.Book;
import com.bookshop.bookshop.service.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    // Key 前缀定义
    private static final String CACHE_BOOK_DETAIL = "book:detail:";
    private static final String CACHE_HOME_LIST = "book:home:list"; // 首页列表 Key
    private static final String CACHE_HOT_SEARCH = "search:hot:keywords";
    /**
     * 场景一：首页列表缓存 (Cache Aside 模式)
     * 缓存整个 List<Book> 的 JSON
     */
    @Override
    public List<Book> getList() {
        // 1. 查 Redis
        String json = stringRedisTemplate.opsForValue().get(CACHE_HOME_LIST);

        if (StringUtils.hasText(json)) {
            try {
                // 复杂类型转换：JSON -> List<Book>
                // 需要使用 TypeReference 告诉 Jackson 这是一个 List
                System.out.println("首页列表走缓存");
                return objectMapper.readValue(json, new TypeReference<List<Book>>(){});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 2. 查数据库 (只查前10本)
        System.out.println("首页列表走数据库");
        List<Book> books = bookMapper.findNewBooks();

        // 3. 写 Redis
        if (books != null && !books.isEmpty()) {
            try {
                String cacheValue = objectMapper.writeValueAsString(books);
                // 首页数据访问极高，且变动不频繁，可以设置稍微久一点，比如 30 分钟
                stringRedisTemplate.opsForValue().set(CACHE_HOME_LIST, cacheValue, 30, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return books;
    }

    /**
     * 场景二：图书详情缓存 (你原本写的逻辑，非常完美，稍微修正了包引用)
     */
    @Override
    public Book getBookById(Integer id) {
        String key = CACHE_BOOK_DETAIL + id;
        String bookJson = stringRedisTemplate.opsForValue().get(key);

        // 1. 命中真实数据
        if (StringUtils.hasText(bookJson)) {
            try {
                System.out.println("图书详情走缓存：" + id);
                return objectMapper.readValue(bookJson, Book.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 2. 命中空值 (防穿透)
        if (bookJson != null) {
            System.out.println("触发防穿透，拦截：" + id);
            return null;
        }

        // 3. 查库
        System.out.println("图书详情走数据库：" + id);
        Book book = bookMapper.findById(id);

        // 4. 处理空结果 (写入空值)
        if (book == null) {
            stringRedisTemplate.opsForValue().set(key, "", 5, TimeUnit.MINUTES);
            return null;
        }

        // 5. 处理正常结果 (写入数据 + 随机过期)
        try {
            String json = objectMapper.writeValueAsString(book);
            long ttl = 30 + (long)(Math.random() * 10);
            stringRedisTemplate.opsForValue().set(key, json, ttl, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return book;
    }

    // 搜索功能通常很难用 Redis 缓存 (因为关键词组合无穷无尽)
    // 暂时直接走数据库 Like 查询
    @Override
    public List<Book> search(String keyword) {
        // 记录热搜，只要用户搜了，就给这个词的分数 +1
        if (StringUtils.hasText(keyword)) {
            stringRedisTemplate.opsForZSet().incrementScore(CACHE_HOT_SEARCH, keyword, 1.0);
        }
        return bookMapper.search(keyword);
    }

    // 获取热搜榜单 (前10名)
    @Override
    public Set<String> getHotSearchKeywords() {
        // Redis 命令: ZREVRANGE search:hot:keywords 0 9
        // 按照分数从大到小排序，取前 10 个
        return stringRedisTemplate.opsForZSet().reverseRange(CACHE_HOT_SEARCH, 0, 9);
    }

    // 假设这是管理员用的接口，还没有实现
    public void updateBook(Book book) {
        // 1. 先更新数据库 (以数据库为准)
        bookMapper.update(book);

        // 2. 删除 "详情页缓存" (下次查询会自动重新加载)
        String detailKey = CACHE_BOOK_DETAIL + book.getId();
        stringRedisTemplate.delete(detailKey);

        // 3. 删除 "首页列表缓存" (防止首页显示旧信息)
        stringRedisTemplate.delete(CACHE_HOME_LIST);

        System.out.println("已同步清理 Redis 缓存");
    }

}