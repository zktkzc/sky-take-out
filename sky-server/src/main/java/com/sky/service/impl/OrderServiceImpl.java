package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrdersPageQueryVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tkzc00
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private AddressBookMapper addressBookMapper;

    @Resource
    private ShoppingCartMapper shoppingCartMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private WebSocketServer webSocketServer;

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
        orderDetailMapper.insertBatch(orderDetailList);
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
        paySuccess(ordersPaymentDTO.getOrderNumber());

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
        Map<String, Object> map = new HashMap();
        map.put("type", 1); // 1-来单提醒，2-用户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "您有新的订单：" + outTradeNo + "，请及时处理");
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 历史订单查询
     *
     * @return
     */
    @Override
    public PageResult list(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrdersPageQueryVO> orders = orderMapper.list(ordersPageQueryDTO);
        for (OrdersPageQueryVO order : orders) {
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
            order.setOrderDetailList(orderDetailList);
        }
        return new PageResult(orders.getTotal(), orders.getResult());
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    @Transactional
    public void again(Long id) {
        Orders orders = orderMapper.getById(id);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.getDefaultByUserId(userId);
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        orders.setAddress(address);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPayStatus(Orders.UN_PAID); // 未付款
        orders.setStatus(Orders.PENDING_PAYMENT); // 待付款
        orders.setCancelTime(null);
        orders.setCancelReason(null);
        orders.setRejectionReason(null);
        orders.setDeliveryTime(null);
        User user = userMapper.getById(userId);
        orders.setUserName(user.getName());
        orderMapper.insert(orders);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orders.getId());
        }
        orderDetailMapper.insertBatch(orderDetailList);
    }

    /**
     * 取消订单
     *
     * @param id 订单id
     */
    @Override
    public void cancle(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason("用户取消")
                .build();
        orderMapper.update(orders);
    }

    /**
     * 根据订单id查询订单详情
     *
     * @param id 订单id
     * @return
     */
    @Override
    public OrdersPageQueryVO listByOrderId(Long id) {
        Orders orders = orderMapper.getById(id);
        OrdersPageQueryVO ordersPageQueryVO = new OrdersPageQueryVO();
        BeanUtils.copyProperties(orders, ordersPageQueryVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        ordersPageQueryVO.setOrderDetailList(orderDetailList);
        return ordersPageQueryVO;
    }

    /**
     * 客户催单
     *
     * @param id 订单id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders != null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2); // 1-来单提醒，2-用户催单
        map.put("orderId", orders.getId());
        map.put("content", "用户催单：" + orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 取消订单
     *
     * @param orders 订单信息
     */
    @Override
    public void cancelOrder(Orders orders) {
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(orders.getCancelReason());
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id 订单id
     */
    @Override
    public void completeOrder(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     *
     * @param orders 订单信息
     */
    @Override
    public void rejection(Orders orders) {
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(orders.getRejectionReason());
        orderMapper.update(orders);
    }

    /**
     * 接单
     *
     * @param orders 订单信息
     */
    @Override
    public void confirm(Orders orders) {
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id 订单id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }
}
