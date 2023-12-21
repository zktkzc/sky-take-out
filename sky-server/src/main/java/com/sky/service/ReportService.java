package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    /**
     * 统计指定时间区间内的营业额
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 营业额统计结果
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 统计指定时间区间内的用户注册量
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 用户注册量统计结果
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
}
