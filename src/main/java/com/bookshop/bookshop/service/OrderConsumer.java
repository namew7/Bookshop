package com.bookshop.bookshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OrderConsumer implements Runnable {

    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private OrderPersistenceService orderPersistenceService;

    private final String STREAM_KEY = "stream.orders";
    private final String GROUP_NAME = "group.order";
    private final String CONSUMER_NAME = "consumer.1";

    @PostConstruct
    public void init() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
        } catch (Exception e) {
            System.out.println("消费者组已就绪");
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 1. 先检查 Pending List (处理旧账)
                List<MapRecord<String, Object, Object>> pendingList = redisTemplate.opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(10),
                        StreamOffset.create(STREAM_KEY, ReadOffset.from("0"))
                );

                if (pendingList != null && !pendingList.isEmpty()) {
                    for (MapRecord<String, Object, Object> record : pendingList) {
                        processRecord(record);
                    }
                    continue; // 继续清理 PEL
                }

                // 2. 再读取新消息
                List<MapRecord<String, Object, Object>> newList = redisTemplate.opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                );

                if (newList != null && !newList.isEmpty()) {
                    for (MapRecord<String, Object, Object> record : newList) {
                        processRecord(record);
                    }
                }

            } catch (Exception e) {
                // 防止Redis连不上导致死循环空转 CPU 100%
                try { Thread.sleep(1000); } catch (InterruptedException ex) {}
            }
        }
    }

    /**
     * 处理消息核心逻辑 (包含死信回滚)
     */
    private void processRecord(MapRecord<String, Object, Object> record) {
        Map<Object, Object> val = record.getValue();
        String orderId = (String) val.get("orderId");
        Integer userId = Integer.valueOf((String) val.get("userId"));
        Integer bookId = Integer.valueOf((String) val.get("bookId"));
        Integer count = Integer.valueOf((String) val.get("count"));

        try {
            // 1. 尝试落库
            orderPersistenceService.createOrderInDB(orderId, userId, bookId, count);

            // 2. 成功则 ACK
            redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, record.getId());

        } catch (Exception e) {
            System.err.println("落库失败: " + e.getMessage());
            // 进入死信判断逻辑
            handleDeadLetter(record, bookId, count);
        }
    }

    /**
     * 死信处理逻辑
     */
    private void handleDeadLetter(MapRecord<String, Object, Object> record, Integer bookId, Integer count) {
        try {
            // 1. 获取该消息的 Pending 详情
            // 注意：返回值类型改为 PendingMessages (它是 List<PendingMessage> 的子类)
            PendingMessages pendingMessages = redisTemplate.opsForStream().pending(
                    STREAM_KEY,
                    Consumer.from(GROUP_NAME, CONSUMER_NAME),
                    Range.closed(record.getId().getValue(), record.getId().getValue()),
                    1
            );

            long deliveryCount = 0;
            // 2. 直接像操作 List 一样操作 pendingMessages
            if (!pendingMessages.isEmpty()) {
                // 直接 get(0) 获取第一条消息，然后获取 deliveryCount
                deliveryCount = pendingMessages.get(0).getTotalDeliveryCount();
            }

            // 2. 判定阈值 (超过 3 次)
            if (deliveryCount > 3) {
                System.err.println("☠️ 判定为死信，执行回滚。MsgId: " + record.getId());

                // A. 移出队列 (ACK) - 防止死循环
                redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, record.getId());

                // B. 回滚 Redis 库存 (Lua 扣掉的要加回来)
                redisTemplate.opsForValue().increment("stock:book:" + bookId, count);
                System.out.println("库存已回滚: bookId=" + bookId + ", count=" + count);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}