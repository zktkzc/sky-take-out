package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrdersPageQueryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("adminOrderController")
@Slf4j
@RequestMapping("/admin/order")
@Api(tags = "管理端订单接口")
public class OrderController {
    @Resource
    private OrderService orderService;

    /**
     * 动态查询订单数据
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("动态查询订单数据")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("动态查询订单：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.list(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 取消订单
     *
     * @param orders 订单信息
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancle(@RequestBody Orders orders) {
        log.info("取消订单：{}", orders);
        orderService.cancelOrder(orders);
        return Result.success();
    }

    /**
     * 完成订单
     *
     * @param id 订单id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id) {
        log.info("完成订单：{}", id);
        orderService.completeOrder(id);
        return Result.success();
    }

    /**
     * 拒单
     *
     * @param orders 订单信息
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody Orders orders) {
        log.info("拒单：{}", orders);
        orderService.rejection(orders);
        return Result.success();
    }

    /**
     * 接单
     *
     * @param orders 订单信息
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody Orders orders) {
        log.info("接单：{}", orders);
        orderService.confirm(orders);
        return Result.success();
    }

    /**
     * 根据id查询订单详情
     *
     * @param id 订单id
     * @return 订单数据
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrdersPageQueryVO> detail(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        OrdersPageQueryVO pageResult = orderService.listByOrderId(id);
        return Result.success(pageResult);
    }

    /**
     * 派送订单
     *
     * @param id 订单id
     * @return
     */
    @PutMapping("delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id) {
        log.info("派送订单：{}", id);
        orderService.delivery(id);
        return Result.success();
    }
}
