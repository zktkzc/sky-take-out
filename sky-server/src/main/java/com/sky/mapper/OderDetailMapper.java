package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OderDetailMapper {
    /**
     * 批量插入订单明细数据
     *
     * @param orderDetailList 订单明细数据列表
     */
    void insertBatch(@Param("orderDetailList") List<OrderDetail> orderDetailList);
}
