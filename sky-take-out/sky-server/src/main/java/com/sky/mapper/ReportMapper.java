package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    Long getTurnover(LocalDate date);

    Long getNewUserCount(LocalDate date);

    long getTotalUserCount(LocalDate date);

    Long getOrderCountByMap(Map map);

    List<GoodsSalesDTO> getSalesTop10(Map map);
}
