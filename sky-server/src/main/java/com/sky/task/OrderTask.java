package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类,处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")// 每分钟执行一次
    //@Scheduled(cron = "1/5 * * * * ?")
    public void processTimeoutOrder() {
        log.info("处理超时订单");
        List<Orders> byStatusAndOrderTimeLT = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if (byStatusAndOrderTimeLT != null && byStatusAndOrderTimeLT.size() > 0) {
            for (Orders orders : byStatusAndOrderTimeLT) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理超时派送订单
     */
    @Scheduled(cron = "0 0 1 * * ?")// 每天凌晨一点执行一次
    //@Scheduled(cron = "0/5 * * * * ?")// 每5秒执行一次
    public void processDeliveryOrder() {
        log.info("处理超时派送订单");
        List<Orders> byStatusAndOrderTimeLT = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if (byStatusAndOrderTimeLT != null && byStatusAndOrderTimeLT.size() > 0) {
            for (Orders orders : byStatusAndOrderTimeLT) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
