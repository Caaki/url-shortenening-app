package com.ares.urlshortening.query;

import com.ares.urlshortening.domain.UrlEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventQuery {
    public static final String SELECT_EVENTS_BY_USER_ID_QUERY=
            "SELECT " +
            "e.description, e.type, ue.id,ue.ip_address,ue.device, ue.browser, ue.created_at " +
            "FROM Events e " +
            "JOIN UserEvents ue ON ue.event_id = e.id " +
            "JOIN users u ON u.id=ue.user_id " +
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
                    "(SELECT id FROM users WHERE email=:email), " +
                    "(SELECT id FROM Events WHERE type=:type)," +
                    " :ipAddress, :device, :browser)";


    public static final String INSERT_URL_EVENT_WITH_USER_ID_QUERY =
            "INSERT INTO UrlEvents" +
            "( user_id, event_id, url_id, ip_address, device, browser) " +
            "VALUES " +
                    "(:userId," +
                    "(SELECT id FROM Events WHERE type=:type)," +
                    ":urlId," +
                    ":ipAddress," +
                    ":device," +
                    ":browser)";

//    @Override
//    public UrlEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
//        return UrlEvent.builder()
//                .id(rs.getLong("id"))
//                .urlId(rs.getLong("url_id"))
//                .userId(rs.getLong("user_id"))
//                .browser(rs.getString("browser"))
//                .device(rs.getString("device"))
//                .description(rs.getString("description"))
//                .ipAddress(rs.getString("ip_address"))
//                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
//                .type(rs.getString("type"))
//                .build();
//    }


    public static final String SELECT_URL_EVENTS_BY_USER_ID_QUERY =
            "SELECT url.alias,url.real_url, url.short_url, url.id as url_id, " +
                    "ue.device, ue.browser, ue.ip_address,ue.id as id, ue.created_at, " +
                    "u.id as user_id " +
                    "FROM urls url  " +
                    "JOIN UrlEvents ue ON url.id = ue.url_id  " +
                    "JOIN users u ON u.id=ue.user_id " +
                    "WHERE u.id = :userId";


}
