package com.markfrain.sample.learnroom.api;

/**
 *
 */

public class HttpException extends Exception {

    private final String responseCode;
    private String responseMsg;

    public HttpException(String responseCode) {
        this.responseCode = responseCode;
    }

    public HttpException(String responseCode, String responseMsg) {
        this.responseCode = responseCode;
        this.responseMsg = responseMsg;
    }

    public String getResponseCode() {
        return responseCode;
    }

    @Override
    public String getMessage() {
        if (responseMsg != null) {
            return responseMsg;
        }
        return super.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        if (responseMsg != null) {
            return responseMsg;
        }
        return super.getLocalizedMessage();
    }
}
