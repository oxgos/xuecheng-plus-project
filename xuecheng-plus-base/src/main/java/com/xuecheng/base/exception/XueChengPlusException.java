package com.xuecheng.base.exception;


/**
 * @description 学成在线项目异常类
 */
public class XueChengPlusException extends RuntimeException {
    private String errMessage;
    // 重写无参构造
    public XueChengPlusException() {
        super();
    }
    // 重写有参构造
    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }
    public String getErrMessage() {
        return errMessage;
    }
    // 自定义的静态方法，使用：XueChengPlusException.cast(CommonError.QUERY_NULL);
    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }
    // 自定义的静态方法，使用：XueChengPlusException.cast("收费规则为空");
    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }
}