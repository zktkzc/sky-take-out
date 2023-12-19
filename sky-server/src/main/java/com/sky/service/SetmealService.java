package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;

import java.util.List;

public interface SetmealService {

    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

    void add(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改套餐
     *
     * @param setmealDTO 套餐信息
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 起售或停售贪残
     *
     * @param status 状态，0-停售，1-起售
     * @param id     套餐id
     */
    void startOrStop(Integer status, Long id);
}
