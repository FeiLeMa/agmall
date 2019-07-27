package com.alag.mmall.controller.backend;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.PropertiesUtil;
import com.alag.mmall.config.RedisService;
import com.alag.mmall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisService redisService;

//    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV1() {
        log.info("开始关闭订单");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.hour", "1"));
        orderService.closeOrder(hour);
        log.info("关闭订单结束");
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV2() {
        String lockTimeout = PropertiesUtil.getProperty("lock.timeout", "50000");
        boolean result = redisService.setNX(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, lockTimeout);
        if (result) {
            log.info("锁已被抢到，执行业务");
            this.closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("锁已被占用");
        }
        log.info("订单关闭任务结束");
    }

    private void closeOrder(String lockName) {
        redisService.expire(lockName, 50000L);
        log.info("设置有效时间成功");
        log.info("开始关闭订单");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.hour", "1"));
        orderService.closeOrder(hour);
        log.info("关闭订单结束");
        redisService.del(lockName);
        log.info("释放锁");
    }


}
