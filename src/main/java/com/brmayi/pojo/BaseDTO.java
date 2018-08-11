package com.brmayi.pojo;

/**
 * Created by xiexiaojing on 2018/8/7.
 */
public class BaseDTO {
    private String reqId;
    private Long reqTimestamp;

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public Long getReqTimestamp() {
        return reqTimestamp;
    }

    public void setReqTimestamp(Long reqTimestamp) {
        this.reqTimestamp = reqTimestamp;
    }
}
