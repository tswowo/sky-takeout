package com.sky.task;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 订单相关定时任务类
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    ShopService shopService;

    /**
     * 处理超时订单
     * 清理下单后十五分钟内未完成支付的订单
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟
    public void processTimeOutOrder() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        //SELECT * FROM order WHERE status = 未支付 AND create_time < 当前时间-15分钟
        List<Orders> timeOutOrders = orderMapper.getByStatusAndCreateTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));

        if (timeOutOrders == null || timeOutOrders.isEmpty()) {
            log.info("没有超时订单");
            return;
        }

        timeOutOrders.forEach(order -> {
            try {
                log.info("处理超时订单,订单号:{}", order.getNumber());
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单支付超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            } catch (Exception e) {
                log.error("处理订单异常：{}", e.getMessage());
            }
        });
        log.info("共处理超时订单{}条", timeOutOrders.size());
    }

    /**
     * 打烊后，每天一点钟检查一次是否有订单处于派送中状态
     * 将派送中订单修改为已完成状态
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单:{}", LocalDateTime.now());
        if (Objects.equals(shopService.getShopStatus().getData(), StatusConstant.ENABLE)) {//如果未打烊
            log.info("当前店铺未打烊，不清理派送中的订单");
            return;
        }
        List<Orders> deliveryOrders = orderMapper.getByStatusAndCreateTime(Orders.DELIVERY_IN_PROGRESS, null);
        if (deliveryOrders == null || deliveryOrders.isEmpty()) {
            log.info("没有处于派送中的订单");
            return;
        }

        deliveryOrders.forEach(order -> {
            try {
                log.info("处理处于派送中的订单,订单号:{}", order.getNumber());
                order.setStatus(Orders.COMPLETED);
                order.setDeliveryTime(LocalDateTime.now());
                orderMapper.update(order);
            } catch (Exception e) {
                log.error("处理订单异常：{}", e.getMessage());
            }
        });
        log.info("共处理{}个处于派送中的订单", deliveryOrders.size());
    }
}
