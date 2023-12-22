package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tkzc00
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间区间内的营业额
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 营业额统计结果
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的用户注册量
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 用户注册量统计结果
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("end", endTime);
            Integer totalCount = userMapper.countByMap(map);
            totalUserList.add(totalCount);
            map.put("begin", beginTime);
            Integer newCount = userMapper.countByMap(map);
            newUserList.add(newCount);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的订单量
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 订单量统计结果
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer totalCount = getOrderCount(beginTime, endTime, null);
            Integer validCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(totalCount);
            validOrderCountList.add(validCount);
        }
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(StringUtils.join(orderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .build();
    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

    /**
     * 统计指定时间区间内的销售额前十的商品
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 销售额前十的商品统计结果
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> top = orderMapper.getSalesTop(beginTime, endTime);
        List<String> nameList = top.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = top.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出营业额统计数据
     *
     * @param response 响应对象
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库，获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        // 通过 POI 将数据写入 Excel 文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet1 = excel.getSheet("Sheet1");
            sheet1.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);
            // 营业额
            sheet1.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            // 订单完成率
            sheet1.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            // 新增用户数
            sheet1.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            // 有效订单
            sheet1.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            // 平均客单价
            sheet1.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());
            // 明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
                BusinessDataVO businessDataVO = workspaceService.getBusinessData(beginTime, endTime);
                // 日期
                sheet1.getRow(7 + i).getCell(1).setCellValue(date.toString());
                // 营业额
                sheet1.getRow(7 + i).getCell(2).setCellValue(businessDataVO.getTurnover());
                // 有效订单
                sheet1.getRow(7 + i).getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                // 订单完成率
                sheet1.getRow(7 + i).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                // 平均客单价
                sheet1.getRow(7 + i).getCell(5).setCellValue(businessDataVO.getUnitPrice());
                // 新增用户数
                sheet1.getRow(7 + i).getCell(6).setCellValue(businessDataVO.getNewUsers());
            }
            // 通过输出流将 Excel 文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            out.close();
            excel.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
