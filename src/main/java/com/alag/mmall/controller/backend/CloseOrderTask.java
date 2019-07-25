package com.alag.mmall.controller.backend;

import com.alag.mmall.common.PropertiesUtil;
import com.alag.mmall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV1() {
        log.info("开始关闭订单");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.hour", "1"));
        orderService.closeOrder(hour);
        log.info("关闭订单结束");
    }
}
