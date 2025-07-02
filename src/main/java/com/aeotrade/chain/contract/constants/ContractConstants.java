package com.aeotrade.chain.contract.constants;

/**
 * @author chenhailong
 */

public enum ContractConstants {

    /**
     * 发布状态：暂存
     */
    RELEASE_STATUS_SAVE("1","暂存"),
    /**
     * 发布状态：已引用
     */
    RELEASE_STATUS_PUBLISHED("2","全部启用"),
    RELEASE_STATUS_PART("4","部分启用"),
    /**
     * 发布状态：已取消引用
     */
    RELEASE_STATUS_UNPUBLISHED("3","已取消引用"),
    /**
     * 执行状态：待启动
     */
    EXECUTE_STATUS_WAIT("1","待启动"),
    /**
     * 执行状态：执行中
     */
    EXECUTE_STATUS_DOING("2","执行中"),
    EXECUTE_STATUS_DOING_WARING("3","执行中（警示）"),
    /**
     * 执行状态：暂停
     */
    EXECUTE_STATUS_TERMINATE("4","暂停"),
    /**
     * 执行状态：执行异常
     */
    EXECUTE_STATUS_EXCEPTION("5","执行异常"),
    /**
     * 执行状态：执行完成
     */
    EXECUTE_STATUS_FINISH("6","执行完成");


    /**
     * 代码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    ContractConstants(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
