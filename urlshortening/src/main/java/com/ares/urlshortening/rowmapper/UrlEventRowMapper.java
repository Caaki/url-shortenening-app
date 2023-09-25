package com.ares.urlshortening.rowmapper;


import com.ares.urlshortening.domain.UrlEvent;
import com.ares.urlshortening.domain.UserEvent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UrlEventRowMapper implements RowMapper<UrlEvent> {
    @Override
    public UrlEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UrlEvent.builder()
                .alisa(rs.getString("alias"))
                .realUrl(rs.getString("real_url"))
                .shortUrl(rs.getString("short_url"))
                .urlId(rs.getLong("url_id"))
                .device(rs.getString("device"))
                .browser(rs.getString("browser"))
                .ipAddress(rs.getString("ip_address"))
                .id(rs.getLong("id"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .userId(rs.getLong("user_id"))
                .build();
    }
}
