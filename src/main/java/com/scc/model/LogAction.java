package com.scc.model;

public final class LogAction {
    private LogAction() {}

    public static final String USER_LOGIN       = "USER_LOGIN";
    public static final String USER_LOGOUT      = "USER_LOGOUT";
    public static final String USER_APPROVED    = "USER_APPROVED";
    public static final String USER_UPDATED     = "USER_UPDATED";
    public static final String BOOKING_CREATED  = "BOOKING_CREATED";
    public static final String BOOKING_APPROVED = "BOOKING_APPROVED";
    public static final String BOOKING_CANCELED = "BOOKING_CANCELED";
    public static final String PAYMENT_RECORDED = "PAYMENT_RECORDED";
    public static final String ROOM_ADDED       = "ROOM_ADDED";
    public static final String ROOM_UPDATED     = "ROOM_UPDATED";
    public static final String ROOM_DELETED     = "ROOM_DELETED";
    public static final String LOGS_CLEARED     = "LOGS_CLEARED";
}