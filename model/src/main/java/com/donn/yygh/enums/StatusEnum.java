package com.donn.yygh.enums;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/8 9:00
 **/
public enum StatusEnum {
    LOCK(0,"锁定"),
    NORMAL(1,"正常")
    ;
    private Integer status;
    private String statusString;

    public static String getStatusStringByStatus(Integer status){
        StatusEnum[] values = StatusEnum.values();
        for (StatusEnum value : values) {
            if (value.getStatus().intValue() == status.intValue()){
                return value.getStatusString();
            }
        }
        return "";
    }

    StatusEnum(Integer status, String statusString) {
        this.status = status;
        this.statusString = statusString;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }
}
