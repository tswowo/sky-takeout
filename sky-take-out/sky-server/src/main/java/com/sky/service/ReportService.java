package com.sky.service;

import com.sky.result.Result;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;


public interface ReportService {
    Result<TurnoverReportVO> turnoverStatistics(String begin, String end);

    Result<UserReportVO> userStatistics(String begin, String end);

    Result<OrderReportVO> ordersStatistics(String begin, String end);

    Result<SalesTop10ReportVO> getSalesTop10(String begin, String end);
}
