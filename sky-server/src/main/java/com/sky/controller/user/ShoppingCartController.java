package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "C端购物车相关接口")
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {
    @Resource
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO 购物车信息
     * @return Result
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车，商品信息为: {}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     *
     * @return 购物车对象列表
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> shoppingCarts = shoppingCartService.showShoppingCart();
        return Result.success(shoppingCarts);
    }

    /**
     * 清空购物车
     *
     * @return Result
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean() {
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    /**
     * 删除购物车中的一个商品
     *
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车中的一个商品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }
}
