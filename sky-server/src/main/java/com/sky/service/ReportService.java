package com.sky.service;

import com.sky.vo.TurnoverReportVO;

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
}
