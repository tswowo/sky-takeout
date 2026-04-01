package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.BaseException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.DistanceCalculator;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private DistanceCalculator distanceCalculator;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Value("${sky.shop.max-distance}")
    private Integer maxDistance;

    /**
     * 历史订单查询
     *
     * @param ordersPageQuery
     * @return Result<PageResult> 查询到的分页信息
     */
    @Override
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQuery) {
        Long userId = BaseContext.getCurrentId();
        //校验参数
        if (ordersPageQuery.getPage() < 1)
            ordersPageQuery.setPage(1);
        if (ordersPageQuery.getPageSize() < 1)
            ordersPageQuery.setPageSize(10);
        ordersPageQuery.setUserId(userId);
        //连表查询 order order_detail ,mybatis自动收集orderDetailList
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQuery);
        //封装结果
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getResult());
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     *
     * @param id 订单id
     * @return Result<OrderVO> 订单详情
     */
    @Override
    public Result<OrderVO> orderDetail(Long id) {
        if (id < 0)
            throw new BaseException("查询订单id异常");
        OrderVO orderVO = orderMapper.orderDetail(id);
        if (orderVO == null)
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        return Result.success(orderVO);
    }

    /**
     * 用户订单提交
     *
     * @param ordersSubmitDTO 订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderSubmitVO> submit(OrdersSubmitDTO ordersSubmitDTO) throws Exception {
        Long userId = BaseContext.getCurrentId();

        //-校验参数
        //检验地址存在
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook query = new AddressBook();
        query.setUserId(userId);
        query.setId(addressBookId);
        List<AddressBook> addressBooks = addressBookMapper.listByAddressBook(query);
        if (addressBooks == null || addressBooks.isEmpty())
            throw new BaseException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        AddressBook addressBook = addressBooks.get(0);
        //检验配送范围
        String address = String.format("%s%s%s%s",
                addressBook.getProvinceName() != null ? addressBook.getProvinceName() : "",
                addressBook.getCityName() != null ? addressBook.getCityName() : "",
                addressBook.getDistrictName() != null ? addressBook.getDistrictName() : "",
                addressBook.getDetail() != null ? addressBook.getDetail() : "");

        double distance;
        try {
            distance = distanceCalculator.calculateDistanceFromShop(address);
        } catch (Exception e) {
            log.error("配送范围校验失败,地址：{}", address, e);
            throw new BaseException("配送范围校验失败，请稍后重试");
        }
        if (distance > maxDistance) {
            log.warn("用户地址超出配送范围，地址：{}, 距离：{} 米", address, distance);
            throw new BaseException(String.format("地址超出配送范围(当前距离:%d 米,最大配送:%d 米)",
                    (int) distance, maxDistance));
        }
        log.info("用户地址在配送范围内,地址：{}, 距离：{} 米", address, distance);
        //校验购物车
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.listByUserId(userId);
        if (shoppingCartList == null || shoppingCartList.isEmpty())
            throw new BaseException(MessageConstant.SHOPPING_CART_IS_NULL);


        //-插入订单表
        Orders orders = new Orders();
        //填充订单信息
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int) ((Math.random() * 9000) + 1000));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getDetail());
        orders.setAddressBookId(addressBook.getId());

        orderMapper.insert(orders);

        //-插入订单详情表
        Long orderId = orders.getId();
        List<OrderDetail> orderDetailList = new ArrayList<>(shoppingCartList.size());
        shoppingCartList.forEach(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orderId);
            orderDetailList.add(orderDetail);
        });
        orderDetailMapper.insertBatch(orderDetailList);

        //-清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        //-封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return Result.success(orderSubmitVO);
    }

    /**
     * 用户订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

        //校验参数
        String orderNumber = ordersPaymentDTO.getOrderNumber();
        Orders orders = orderMapper.getByNumber(orderNumber);
        if (orders == null)
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        if (!Objects.equals(orders.getPayStatus(), Orders.UN_PAID))
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        if (!Objects.equals(orders.getStatus(), Orders.PENDING_PAYMENT))
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);

        //没有商户就就跳过支付(前端:重定向到支付成功)，直接更新状态(后端直接调用paySuccess来实现状态的更新)
        paySuccess(ordersPaymentDTO.getOrderNumber());

        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 用户再来一单
     *
     * @param orderId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result repetition(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        //获取订单信息
        Orders orders = orderMapper.getById(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(orderId);
        //校验订单信息
        if (orders == null || !Objects.equals(userId, orders.getUserId()))
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        if (orderDetailList == null || orderDetailList.isEmpty())
            throw new BaseException(MessageConstant.SHOPPING_CART_IS_NULL);
        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        //插入到购物车
        List<ShoppingCart> shoppingCartList = new ArrayList<>(orderDetailList.size());
        orderDetailList.forEach(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCartList.add(shoppingCart);
        });
        shoppingCartMapper.insertBatch(shoppingCartList);

        return Result.success();
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result userCancelOrder(Long id) {
        Long userId = BaseContext.getCurrentId();
        //校验订单信息
        Orders orders = orderMapper.getById(id);
        if (orders == null || !Objects.equals(userId, orders.getUserId()))
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        if (!Objects.equals(orders.getStatus(), Orders.PENDING_PAYMENT) && !Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED))
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);

        cancelOrder(orders, "用户取消订单");
        return Result.success();
    }

    /**
     * 订单条件查询
     *
     * @param ordersPageQuery
     * @return
     */
    @Override
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQuery) {
        //校验参数
        if (ordersPageQuery.getPage() < 1)
            ordersPageQuery.setPage(1);
        if (ordersPageQuery.getPageSize() < 1)
            ordersPageQuery.setPageSize(10);
        //连表查询 order order_detail ,mybatis自动收集orderDetailList
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQuery);
        //封装结果
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getResult());
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return 订单数量统计 包含带派送、派送中、待接单
     */
    @Override
    public Result<OrderStatisticsVO> statistics() {
        Integer toBeConfirmedCount = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmedCount = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgressCount = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmedCount);
        orderStatisticsVO.setConfirmed(confirmedCount);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgressCount);
        return Result.success(orderStatisticsVO);
    }

    /**
     * 商家确认接单
     *
     * @param ordersConfirmDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //校验参数
        Long orderId = ordersConfirmDTO.getId();
        Orders orders = orderMapper.getById(orderId);
        if (orders == null)
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        if (!Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED)
                || !Objects.equals(orders.getPayStatus(), Orders.PAID))
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        //确认接单
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
        return Result.success();
    }

    /**
     * 商家拒绝接单
     *
     * @param ordersRejectionDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //校验参数
        Long orderId = ordersRejectionDTO.getId();
        Orders orders = orderMapper.getById(orderId);
        if (orders == null)
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        if (!Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED))
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        //退款
        if (Objects.equals(orders.getPayStatus(), Orders.PAID)) {
            //some codes...
            orders.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(orders);
        return Result.success();
    }

    /**
     * 商家取消订单
     *
     * @param ordersRejectionDTO 商家取消的订单和原因 数据模型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result adminCancelOrder(OrdersRejectionDTO ordersRejectionDTO) {
        //校验订单信息
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null)
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        if (Objects.equals(orders.getStatus(), Orders.CANCELLED))//如果订单已取消
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);

        cancelOrder(orders, ordersRejectionDTO.getCancelReason());
        return Result.success();
    }

    /**
     * 商家派送订单
     *
     * @param id
     * @return
     */
    @Override
    public Result delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null)
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        if (!Objects.equals(orders.getStatus(), Orders.CONFIRMED))
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
        return Result.success();
    }

    /**
     * 商家完成订单
     *
     * @param id
     * @return
     */
    @Override
    public Result complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null)
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        if (!Objects.equals(orders.getStatus(), Orders.DELIVERY_IN_PROGRESS))
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
        return Result.success();
    }

    /**
     * 工具方法 取消订单
     *
     * @param orders       要取消的订单信息
     * @param cancelReason 取消原因s
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Orders orders, String cancelReason) {
        //如果是已支付，则需要退款
        if (Objects.equals(orders.getPayStatus(), Orders.PAID)) {
            //按理说这里应该调wx的退款，但是没有商户的小程序，所以只能模拟一下了
            //some codes....

            //将支付状态改为已退款
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(cancelReason);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
}
