package com.sky.dto.baidu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {

    private Location location;
    private Integer precise;
    private Integer confidence;
    private Integer comprehension;
    private String level;

}
