package com.donn.yygh.hosp.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/25 19:22
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Actor {
    private String id;
    private String name;
    private Integer age;
    private boolean gender;
}
