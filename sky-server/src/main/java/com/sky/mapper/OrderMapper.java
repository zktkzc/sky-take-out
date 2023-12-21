package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrdersPageQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     *
     * @param orders 订单
     */
    void insert(Orders orders);

    /**
     * 根据订单号和用户id查询订单
     *
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(@Param("orderNumber") String orderNumber, @Param("userId") Long userId);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据订单状态和下单时间查询订单
     *
     * @param status    订单状态
     * @param orderTime 下单时间
     * @return 订单列表
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(@Param("status") Integer status, @Param("orderTime") LocalDateTime orderTime);

    /**
     * 根据订单号查询订单id
     *
     * @param orderNumber 订单号
     * @return 订单id
     */
    @Select("select id from orders where number = #{orderNumber}")
    Long getIdByNumber(String orderNumber);

    /**
     * 历史订单查询
     *
     * @param ordersPageQueryDTO 订单查询条件
     * @return 订单列表
     */
    Page<OrdersPageQueryVO> list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单数据
     *
     * @param orderId 订单id
     * @return
     */
    @Select("select * from orders where id = #{orderId}")
    Orders getById(Long orderId);

    /**
     * 根据动态条件统计营业额数据
     *
     * @param map 动态条件
     * @return 营业额
     */
    Double sumByMap(Map<String, Object> map);
}
