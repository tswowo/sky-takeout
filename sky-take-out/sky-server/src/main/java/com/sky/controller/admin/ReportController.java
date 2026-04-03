package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController("adminReportController")
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "管理端数据统计相关接口")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @ApiOperation("营业额统计接口")
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(@Parameter String begin, @Parameter String end) {
        log.info("营业额统计");
        return reportService.turnoverStatistics(begin, end);
    }

    @ApiOperation("用户统计接口")
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(@Parameter String begin, @Parameter String end) {
        log.info("用户统计");
        return reportService.userStatistics(begin, end);
    }

    @ApiOperation("订单统计接口")
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(@Parameter String begin, @Parameter String end) {
        log.info("订单统计");
        return reportService.ordersStatistics(begin, end);
    }

    @ApiOperation("查询销量排名top10接口")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(@Parameter String begin, @Parameter String end) {
        log.info("查询销量排名top10");
        return reportService.getSalesTop10(begin, end);
    }

}
