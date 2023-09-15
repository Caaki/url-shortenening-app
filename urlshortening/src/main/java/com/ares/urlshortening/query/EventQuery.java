package com.ares.urlshortening.query;

public class EventQuery {
    public static final String SELECT_EVENTS_BY_USER_ID_QUERY=
            "SELECT " +
            "e.description, e.type, ue.id,ue.ip_address,ue.device, ue.browser, ue.created_at " +
            "FROM Events e " +
            "JOIN UserEvents ue ON ue.user_id = e.id " +
            "JOIN Users u ON u.id=ue.user_id " +
            "WHERE u.id=:userId " +
            "ORDER BY ue.created_at DESC " +
            "LIMIT 10";
    public static final String INSERT_USER_EVENT_WITH_USER_ID_QUERY =
            "INSERT INTO UserEvents (user_id, event_id, ip_address, device, browser)" +
            "VALUES " +
                    "(:userId, " +
                    "(SELECT id FROM Events WHERE type=:type), " +
                    ":ipAddress, " +
                    ":device, " +
                    ":browser)";
    public static final String INSERT_USER_EVENT_WITH_USER_EMAIL_QUERY =
            "INSERT INTO UserEvents (user_id, event_id, ip_address, device, browser) " +
                    "VALUES " +
                    "(SELECT id FROM Users WHERE email=:email), " +
                    "(SELECT id FROM Events WHERE type=:type)," +
                    " :ipAddress, :device, :browser)";

}
