package org.cloud.sonic.common.gitUtils.exception;

/**
 * @description: 消息自定义异常
 * @author:
 * @date: 2019/9/13 17:21
 */
public class MsgException extends Exception {

    public static enum ErrEnum {
        SSLException,
        IOException,
        HttpHostConnectException,

        //Helium
        NotFound;

    }


    public MsgException() {
    }

    ErrEnum errEnum;

    public ErrEnum getErrEnum() {
        return errEnum;
    }

    public void setErrEnum(ErrEnum errEnum) {
        this.errEnum = errEnum;
    }

    public MsgException(String message) {
        super(message);
    }

    public MsgException(String message, ErrEnum errEnum) {
        super(message);
        this.errEnum = errEnum;
    }

    public MsgException(String message, ErrEnum errEnum, Throwable cause) {
        super(message, cause);
        this.errEnum = errEnum;
    }

    public MsgException(String message, Throwable cause) {
        super(message, cause);
    }

    public MsgException(Throwable cause) {
        super(cause);
    }

    public MsgException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
