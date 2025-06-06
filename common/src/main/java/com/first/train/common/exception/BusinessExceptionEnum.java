package com.first.train.common.exception;

public enum BusinessExceptionEnum {


    MEMBER_MOBILE_EXIST("手机号已存在"),
    MEMBER_MOBILE_Not_EXIST("请先获取短信验证"),
    MEMBER_MOBILE_Code_ERROR("短信验证错误");




    private String desc;

    BusinessExceptionEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "BusinessExceptionEnum{" +
                "desc='" + desc + '\'' +
                '}';
    }
}
