package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询套餐id
     *
     * @param dishIds 菜品id列表
     * @return 套餐id列表
     */
    List<Long> getSetMealIdsByDishId(@Param("dishIds") List<Long> dishIds);

    void insert(SetmealDish setmealDish);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getDishBySetmealId(Long setmealId);

    /**
     * 根据套餐id删除菜品
     *
     * @param setmealId 套餐id
     */
    void deleteBySetmealId(Long setmealId);
}
