package com.aeotrade.chain.contract.vo;

public enum StatusEnum {

    /*
     * U:user
     * E:enterprise
     * */

    SUCCESS("0000", "成功"),
    SUCCESS_NO_DATA("1000", "没有相关数据"),
    UPDATE_FAIL("-2000", "保存失败"),
    PARAMETER_DISCREPANCY("-1001", "参数不符合规范"),
    PARAMETER_EXCEPTION("-1002", "请检查必填项"),
    OPERATION_NOT_ALLOWED("-1000", "不允许的操作");



    private String code;

    private String message;

    StatusEnum() {
    }

    StatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return message;
    }

}

