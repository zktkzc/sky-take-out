package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Resource
    private DishService dishService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId 分类id
     * @return 菜品列表
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        // 构造redis中的key
        String key = "dish_" + categoryId;
        // 查询redis中是否存在菜品数据
        ValueOperations valueOperations = redisTemplate.opsForValue();
        List<DishVO> list = (List<DishVO>) valueOperations.get(key);
        if (list != null && list.size() > 0) {
            // 存在，直接返回
            return Result.success(list);
        }
        // 不存在，查询数据库，将查询到的数据保存到redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);// 查询起售中的菜品
        list = dishService.listWithFlavor(dish);
        valueOperations.set(key, list);
        return Result.success(list);
    }
}
