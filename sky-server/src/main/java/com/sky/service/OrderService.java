package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrdersPageQueryVO;

public interface OrderService {
    /**
     * 再来一单
     *
     * @param id 订单id
     */
    void again(Long id);

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO 订单提交信息
     * @return 订单提交结果
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 历史订单查询
     *
     * @return 订单列表
     */
    PageResult list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 取消订单
     *
     * @param id 订单id
     */
    void cancle(Long id);

    /***
     * 根据订单id查询订单详情
     * @param id 订单id
     * @return
     */
    OrdersPageQueryVO listByOrderId(Long id);
}
