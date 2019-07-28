package com.alag.mmall.controller.backend;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.PropertiesUtil;
import com.alag.mmall.config.RedisService;
import com.alag.mmall.config.RedissonManager;
import com.alag.mmall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RedissonManager redissonManager;

//    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV1() {
        log.info("开始关闭订单");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.hour", "1"));
        orderService.closeOrder(hour);
        log.info("关闭订单结束");
    }

//    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV2() {
        String lockTimeout = PropertiesUtil.getProperty("lock.timeout", "50000");
        boolean result = redisService.setNX(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, System.currentTimeMillis()+lockTimeout);
        if (result) {
            log.info("锁已被抢到，执行业务");
            this.closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("锁已被占用");
        }
        log.info("订单关闭任务结束");
    }

//    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3() {
        Long lockTimeout = Long.valueOf(PropertiesUtil.getProperty("lock.timeout", "5000"));
        boolean result = redisService.setNX(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis()+lockTimeout));
        if (result) {
            log.info("锁已被抢到，执行业务");
            this.closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("锁已被占用，尝试重置再次获取");
            Object objret =  redisService.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if (objret != null && System.currentTimeMillis() > Long.valueOf(objret.toString())) {
                byte[] ret = redisService.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis()+lockTimeout));
                //ret返回null的时候说明这个锁已经不存在，那么会直接set，获取锁

                if (ret == null || objret.toString().equals(new String(ret))) {
                    log.info("重置，获得锁");
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                } else {

                    log.info("重置获取锁失败");
                }
            } else {
                log.info("时间未超时，获取失败");
            }
        }
        log.info("订单关闭任务结束");
    }
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV4() {
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            if (getLock = lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                log.info("获取到Redisson分布式锁");
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.hour", "1"));
                orderService.closeOrder(hour);
            } else {
                log.info("Redisson没有获取到分布式锁");
            }
        } catch (InterruptedException e) {
            log.error("Redisson 分布式锁异常");
        }finally {
            if (!getLock) {
                return;
            }
            lock.unlock();
            log.info("Redisson分布式锁已被释放");
        }
    }



        private void closeOrder(String lockName) {
        redisService.expire(lockName, 50L);
        log.info("设置有效时间成功");
        log.info("开始关闭订单");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.hour", "1"));
        orderService.closeOrder(hour);
        log.info("关闭订单结束");
        redisService.del(lockName);
        log.info("释放锁");
    }





}
