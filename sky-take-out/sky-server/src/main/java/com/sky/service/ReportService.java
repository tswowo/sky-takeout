package com.sky.service;

import com.sky.result.Result;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;


public interface ReportService {
    Result<TurnoverReportVO> turnoverStatistics(LocalDate begin, LocalDate end);

    Result<UserReportVO> userStatistics(LocalDate begin, LocalDate end);

    Result<OrderReportVO> ordersStatistics(LocalDate begin, LocalDate end);

    Result<SalesTop10ReportVO> getSalesTop10(LocalDate begin, LocalDate end);

    void exportBussinessData(HttpServletResponse response);
}
