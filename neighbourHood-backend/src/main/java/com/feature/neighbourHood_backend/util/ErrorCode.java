package com.feature.neighbourHood_backend.util;

public class ErrorCode {
    // Auth Errors 1xxx
    public static final String AUTH_INVALID_CREDENTIALS = "1001";
    public static final String AUTH_EMAIL_ALREADY_EXISTS = "1002";
    public static final String AUTH_INVALID_EMAIL_FORMAT = "1003";
    public static final String AUTH_PASSWORD_TOO_SHORT = "1004";
    public static final String AUTH_USER_NOT_FOUND = "1005";
    public static final String AUTH_ROLE_NOT_FOUND = "1006";
    public static final String AUTH_TOKEN_INVALID = "1007";
    public static final String AUTH_TOKEN_EXPIRED = "1008";
    public static final String AUTH_UNAUTHORIZED = "1009";
    public static final String AUTH_USERNAME_ALREADY_EXISTS = "1010";
    public static final String AUTH_MISSING_REQUIRED_FIELDS = "1011";

    // Validation Errors 2xxx
    public static final String VALIDATION_NAME_EMPTY = "2001";
    public static final String VALIDATION_AGE_INVALID = "2002";
    public static final String VALIDATION_HKID_INVALID = "2003";
    public static final String VALIDATION_ADDRESS_EMPTY = "2004";
    public static final String VALIDATION_EMAIL_INVALID = "2005";
    public static final String VALIDATION_PASSWORD_WEAK = "2006";

    // Database Errors 3xxx
    public static final String DATABASE_ERROR = "3001";
    public static final String DATABASE_DUPLICATE_KEY = "3002";
    public static final String DATABASE_RECORD_NOT_FOUND = "3003";
    public static final String DATABASE_CONNECTION_ERROR = "3004";

    // User Profile Errors 4xxx
    public static final String USER_PROFILE_NOT_FOUND = "4001";
    public static final String USER_EMAIL_ALREADY_REGISTERED = "4002";
    public static final String USER_UPDATE_FAILED = "4003";
    public static final String USER_DELETE_FAILED = "4004";

    // Post Errors 5xxx
    public static final String POST_NOT_FOUND = "5001";
    public static final String POST_CREATE_FAILED = "5002";
    public static final String POST_UPDATE_FAILED = "5003";
    public static final String POST_DELETE_FAILED = "5004";
    public static final String POST_CONTENT_EMPTY = "5005";
    public static final String POST_IMAGE_UPLOAD_FAILED = "5006";

    // General Errors 9xxx
    public static final String INTERNAL_SERVER_ERROR = "9001";
    public static final String BAD_REQUEST = "9002";
    public static final String RESOURCE_NOT_FOUND = "9003";
    public static final String REQUEST_TIMEOUT = "9004";

    // Success Codes 2xx
    public static final String SUCCESS = "200";
    public static final String CREATED = "201";
    public static final String ACCEPTED = "202";
}
