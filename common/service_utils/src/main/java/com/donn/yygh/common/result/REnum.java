package com.donn.yygh.common.result;



/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/22 9:49
 **/
//封装结果对象，需要使用的枚举类
//枚举类是为了规范
public enum REnum {
    SUCCESS(20000,true,"成功"),
    ERROR(20001,false,"失败")
    ;
    private Integer code;
    private Boolean success;
    private String message;

    REnum(Integer code, Boolean success, String message) {
        this.code = code;
        this.success = success;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getSuccess() {
        return success;
    }
}
