package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return Result<TurnoverReportVO> 查询日期范围内每天的营业额
     */
    @Override
    public Result<TurnoverReportVO> turnoverStatistics(LocalDate begin, LocalDate end) {
        if (begin == null || end == null)
            return Result.error("开始日期或结束日期不能为空");
        if (begin.isAfter(end))
            return Result.error("开始日期不能大于结束日期");

        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            dateList.add(date.toString());

            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            map.put("end", LocalDateTime.of(date, LocalTime.MAX));

            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
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
    public Result<UserReportVO> userStatistics(LocalDate begin, LocalDate end) {
        if (begin == null || end == null)
            return Result.error("开始日期或结束日期不能为空");
        if (begin.isAfter(end))
            return Result.error("开始日期不能大于结束日期");

        List<String> dateList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();

        LocalDate date = begin;
        HashMap<String, Object> map = new HashMap<>();
        while (!date.isAfter(end)) {
            dateList.add(date.toString());

            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            map.put("end", LocalDateTime.of(date, LocalTime.MAX));
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser.toString());

            map.put("begin", null);
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser.toString());

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
    public Result<OrderReportVO> ordersStatistics(LocalDate begin, LocalDate end) {
        if (begin == null || end == null)
            return Result.error("开始日期或结束日期不能为空");
        if (begin.isAfter(end))
            return Result.error("开始日期不能大于结束日期");

        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        LocalDate date = begin;
        int totalOrderCount = 0;
        int validTotalOrderCount = 0;
        while (!date.isAfter(end)) {
            dateList.add(date.toString());

            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            map.put("end", LocalDateTime.of(date, LocalTime.MAX));

            map.put("status", null);
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount.toString());
            totalOrderCount += orderCount;

            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
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
    public Result<SalesTop10ReportVO> getSalesTop10(LocalDate begin, LocalDate end) {
        if (begin == null || end == null)
            return Result.error("开始日期或结束日期不能为空");
        if (begin.isAfter(end))
            return Result.error("开始日期不能大于结束日期");

        List<String> nameList = new ArrayList<>();
        List<String> numberList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("end", LocalDateTime.of(end, LocalTime.MAX));
        map.put("status", Orders.COMPLETED);
        List<GoodsSalesDTO> list = orderMapper.getSalesTop10(map);
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

    /**
     * 导出营业数据
     *
     * @param response
     */
    @Override
    public void exportBussinessData(HttpServletResponse response) {
        //查询营业数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        //写入到excel
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            Workbook workbook = new XSSFWorkbook(in);
            Sheet sheet = workbook.getSheet("Sheet1");
            //填充时间
            sheet.getRow(1).getCell(1).setCellValue("时间:" + begin + "至" + end);

            //获取30天汇总数据
            BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                    LocalDateTime.of(begin, LocalTime.MIN),
                    LocalDateTime.of(end, LocalTime.MAX)
            );

            //填充概览数据
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //获取每日数据
                BusinessDataVO dailyBusinessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX)
                );

                Row row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dailyBusinessData.getTurnover());
                row.getCell(3).setCellValue(dailyBusinessData.getValidOrderCount());
                row.getCell(4).setCellValue(dailyBusinessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dailyBusinessData.getUnitPrice());
                row.getCell(6).setCellValue(dailyBusinessData.getNewUsers());
            }

            //设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=businessData.xlsx");

            //输出到客户端
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();

            //关闭资源
            out.close();
            workbook.close();
            log.info("导出成功");
        } catch (IOException e) {
            log.error("导出运营数据失败", e);
        }
    }

}
