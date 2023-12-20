package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 查询购物车
     *
     * @param shoppingCart 购物车实体类
     * @return 购物车实体类
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id更新购物车数量
     *
     * @param shoppingCart 购物车实体类
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 添加购物车
     *
     * @param shoppingCart 购物车实体类
     */
    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id清空购物车
     *
     * @param userId 用户id
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    @Delete("delete from shopping_cart where id = #{id} and user_id = #{userId}")
    void delete(ShoppingCart shoppingCart);
}
