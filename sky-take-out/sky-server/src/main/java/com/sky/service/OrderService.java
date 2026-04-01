package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQuery);

    Result<OrderVO> orderDetail(Long id);

    Result<OrderSubmitVO> submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    Result repetition(Long orderId);

    Result userCancelOrder(Long id);

    Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQuery);

    Result<OrderStatisticsVO> statistics();

    Result confirm(OrdersConfirmDTO ordersConfirmDTO);

    Result rejection(OrdersRejectionDTO ordersRejectionDTO);

    Result adminCancelOrder(OrdersRejectionDTO ordersRejectionDTO);

    Result delivery(Long id);

    Result complete(Long id);
}
