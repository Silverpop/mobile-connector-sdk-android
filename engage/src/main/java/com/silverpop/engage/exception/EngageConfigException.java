package com.silverpop.engage.exception;

/**
 * Created by Lindsay Thurmond on 1/2/15.
 */
public class EngageConfigException extends Exception {

    public EngageConfigException() {
    }

    public EngageConfigException(String detailMessage) {
        super(detailMessage);
    }

    public EngageConfigException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public EngageConfigException(Throwable throwable) {
        super(throwable);
    }

}
