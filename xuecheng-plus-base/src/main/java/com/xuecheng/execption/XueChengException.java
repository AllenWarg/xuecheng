package com.xuecheng.execption;

/**
 * @Author gc
 * @Description 自定义异常类
 * @DateTime: 2025/5/14 0:12
 **/
public class XueChengException extends RuntimeException {
    private String errMessage;

    public XueChengException(){
        super();
    }

    public XueChengException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
    public static void cast(CommonError commonError){
        throw new XueChengException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new XueChengException(errMessage);
    }

}
