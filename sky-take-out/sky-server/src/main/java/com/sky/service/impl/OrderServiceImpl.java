package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.BaseException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

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
     * 订单提交
     *
     * @param ordersSubmitDTO 订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderSubmitVO> submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        //-校验参数
        //检验地址
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook query = new AddressBook();
        query.setUserId(userId);
        query.setId(addressBookId);
        List<AddressBook> addressBooks = addressBookMapper.listByAddressBook(query);
        if (addressBooks == null || addressBooks.isEmpty())
            throw new BaseException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        AddressBook addressBook = addressBooks.get(0);
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
     * 订单支付
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
     * 再来一单
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
}
