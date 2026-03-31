package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

    /**
     * 历史订单查询
     *
     * @param ordersPageQuery 查询参数
     * @return 订单列表,封装了orderDetailList
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQuery);

    /**
     * 订单详情
     *
     * @param id 订单id
     * @return 订单详情,封装了orderDetailList
     */
    OrderVO orderDetail(Long id);

    /**
     * 创建新下的订单到订单表
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);


    /**
     * 根据id查询订单信息
     *
     * @param id
     */
    Orders getById(Long id);

}
