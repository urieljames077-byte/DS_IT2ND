package com.scc.model;

public record Booking(int id, int tenantId, int roomId, String startDate,
                      String endDate, String status) {
}