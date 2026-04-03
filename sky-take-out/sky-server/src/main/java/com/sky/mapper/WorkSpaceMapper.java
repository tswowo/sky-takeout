package com.sky.mapper;

import com.sky.vo.BusinessDataVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;


@Mapper
public interface WorkSpaceMapper {


    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);
}
