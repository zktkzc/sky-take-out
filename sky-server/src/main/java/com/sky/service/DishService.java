package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO 菜品信息
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO 查询条件
     * @return PageResult
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     *
     * @param ids 菜品id列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和对应的口味数据
     *
     * @param id 菜品id
     * @return 菜品信息
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品信息和口味信息
     *
     * @param dishDTO 菜品数据
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    /**
     * 启用或禁用菜品
     *
     * @param status 状态
     * @param id     菜品id
     */
    void startOrStop(Integer status, Long id);
}
