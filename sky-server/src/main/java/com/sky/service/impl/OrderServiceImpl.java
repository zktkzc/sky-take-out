package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tkzc00
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OderDetailMapper oderDetailMapper;

    @Resource
    private AddressBookMapper addressBookMapper;

    @Resource
    private ShoppingCartMapper shoppingCartMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO 订单提交信息
     * @return 订单提交结果
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理业务异常（地址簿为空、购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null)
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty())
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        // 向订单表出入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now()); // 下单时间
        orders.setPayStatus(Orders.UN_PAID); // 未付款
        orders.setStatus(Orders.PENDING_PAYMENT); // 待付款
        orders.setNumber(String.valueOf(System.currentTimeMillis())); // 订单号
        orders.setPhone(addressBook.getPhone()); // 用户手机号
        orders.setConsignee(addressBook.getConsignee()); // 收货人
        orders.setUserId(userId); // 用户id
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        orders.setAddress(address); // 收货地址
        User user = userMapper.getById(userId);
        orders.setUserName(user.getName()); // 用户名
        orderMapper.insert(orders);
        // 向订单明细表出入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        oderDetailMapper.insertBatch(orderDetailList);
        // 清空用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        // 封装VO返回结果
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount()).build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        // 调用微信支付接口，生成预支付交易单
        // JSONObject jsonObject = weChatPayUtil.pay(
        //         ordersPaymentDTO.getOrderNumber(), // 商户订单号
        //         new BigDecimal(0.01), // 支付金额，单位 元
        //         "苍穹外卖订单", // 商品描述
        //         user.getOpenid() // 微信用户的openid
        // );
        //
        // if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
        //     throw new OrderBusinessException("该订单已支付");
        // }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        orderMapper.updateStatus(Orders.PAID, Orders.TO_BE_CONFIRMED, LocalDateTime.now(), ordersPaymentDTO.getOrderNumber());

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }
}
