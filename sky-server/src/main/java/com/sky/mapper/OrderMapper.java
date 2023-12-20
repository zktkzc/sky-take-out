package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

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

    @Update("update orders set pay_status = #{orderPaidStatus}, status = #{orderStatus}, checkout_time = #{checkoutTime} where number = #{orderNumber}")
    void updateStatus(@Param("orderPaidStatus") Integer orderPaidStatus, @Param("orderStatus") Integer orderStatus, @Param("checkoutTime") LocalDateTime checkoutTime, @Param("orderNumber") String orderNumber);
}
