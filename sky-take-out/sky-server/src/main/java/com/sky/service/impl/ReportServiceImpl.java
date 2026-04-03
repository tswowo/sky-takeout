package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;

    /**
     * 营业额统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return Result<TurnoverReportVO> 查询日期范围内每天的营业额
     */
    @Override
    public Result<TurnoverReportVO> turnoverStatistics(String begin, String end) {
        if (begin == null || end == null)
            return Result.error("开始日期或结束日期不能为空");
        LocalDate beginDate = LocalDate.parse(begin);
        LocalDate endDate = LocalDate.parse(end);
        if (beginDate.isAfter(endDate))
            return Result.error("开始日期不能大于结束日期");

        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();
        LocalDate date = beginDate;
        while (!date.isAfter(endDate)) {
            dateList.add(date.toString());
            Long turnover = reportMapper.getTurnover(date);
            turnover = turnover != null ? turnover : 0L;

            turnoverList.add(turnover.toString());
            date = date.plusDays(1);
        }
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
        log.info("营业额统计结果：{}", turnoverReportVO);
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return Result<UserReportVO> 查询日期范围内每天的用户新增数和当天的总用户数
     */
    @Override
    public Result<UserReportVO> userStatistics(String begin, String end) {
        if (begin == null || end == null)
            return Result.error("开始日期或结束日期不能为空");
        LocalDate beginDate = LocalDate.parse(begin);
        LocalDate endDate = LocalDate.parse(end);
        if (beginDate.isAfter(endDate))
            return Result.error("开始日期不能大于结束日期");

        List<String> dateList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();

        LocalDate date = beginDate;
        long totalUser = 0;
        while (!date.isAfter(endDate)) {
            dateList.add(date.toString());

            Long newUser = reportMapper.getNewUserCount(date);
            newUser = newUser != null ? newUser : 0L;
            newUserList.add(newUser.toString());

            if (Objects.equals(date, beginDate))
                totalUser = reportMapper.getTotalUserCount(date);
            else totalUser += newUser;
            totalUserList.add(Long.toString(totalUser));

            date = date.plusDays(1);
        }
        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .newUserList(String.join(",", newUserList))
                .totalUserList(String.join(",", totalUserList))
                .build();
        log.info("用户统计结果：{}", userReportVO);

        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return Result<OrderStatisticsVO> 查询日期范围内每天的订单完成率 订单总数 订单列表 有效订单数 有效订单列表
     */
    @Override
    public Result<OrderReportVO> ordersStatistics(String begin, String end) {
        if (begin == null || end == null)
            return Result.error("开始日期或结束日期不能为空");
        LocalDate beginDate = LocalDate.parse(begin);
        LocalDate endDate = LocalDate.parse(end);
        if (beginDate.isAfter(endDate))
            return Result.error("开始日期不能大于结束日期");

        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        LocalDate date = beginDate;
        int totalOrderCount = 0;
        int validTotalOrderCount = 0;
        while (!date.isAfter(endDate)) {
            dateList.add(date.toString());
            map.put("date", date);
            map.put("status", null);
            Long orderCount = reportMapper.getOrderCountByMap(map);
            orderCount = orderCount != null ? orderCount : 0L;
            orderCountList.add(orderCount.toString());
            totalOrderCount += orderCount;

            map.put("status", Orders.COMPLETED);
            Long validOrderCount = reportMapper.getOrderCountByMap(map);
            validOrderCount = validOrderCount != null ? validOrderCount : 0L;
            validOrderCountList.add(validOrderCount.toString());
            validTotalOrderCount += validOrderCount;

            date = date.plusDays(1);
        }
        double orderCompletionRate = validTotalOrderCount * 1.0 / totalOrderCount;
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validTotalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
        log.info("订单统计结果：{}", orderReportVO);

        return Result.success(orderReportVO);
    }

    /**
     * 销量排名前十商品
     *
     * @param begin
     * @param end
     * @return Result<SalesTop10ReportVO> 商品名称列表 销量列表
     */
    @Override
    public Result<SalesTop10ReportVO> getSalesTop10(String begin, String end) {
        if (begin == null || end == null)
            return Result.error("开始日期或结束日期不能为空");
        LocalDate beginDate = LocalDate.parse(begin);
        LocalDate endDate = LocalDate.parse(end);
        if (beginDate.isAfter(endDate))
            return Result.error("开始日期不能大于结束日期");

        List<String> nameList = new ArrayList<>();
        List<String> numberList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("begin", beginDate.atStartOfDay());
        map.put("end", endDate.atTime(LocalTime.MAX));
        map.put("status", Orders.COMPLETED);
        List<GoodsSalesDTO> list = reportMapper.getSalesTop10(map);
        for (GoodsSalesDTO dto : list) {
            nameList.add(dto.getName());
            numberList.add(dto.getNumber().toString());
        }

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
        log.info("销量排名结果：{}", salesTop10ReportVO);

        return Result.success(salesTop10ReportVO);
    }

}
