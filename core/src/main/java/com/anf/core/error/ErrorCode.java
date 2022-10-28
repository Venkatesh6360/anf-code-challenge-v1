package com.anf.core.error;

/**
 * Enum defines all the errors expected from the application. Each enum value
 * has a unique error code, error message and appropriate http status code used
 * for that error.
 * 
 * @author Venkatesh K A
 *
 */
public enum ErrorCode {

    RUNTIME_ERROR("100", "Request encountered an unexpected error", 500), 
    USER_FIELDS_MISSING("101", "Values for one or more attributes are not provided", 400), 
    AGE_CONFIG_ISSUE("102", "Unable to read age configuration for validation", 500), 
    USER_AGE_INELIGIBLE("103", "You are not eligible", 400);

    /** The error code. */
    private String code;

    /** The error message. */
    private String message;

    /** The http status code */
    private int statusCode;

    /**
     * Instantiates a new error code.
     *
     * @param code
     *            the error code
     * @param message
     *            the error message
     */
    ErrorCode(String code, String message, int statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

}
