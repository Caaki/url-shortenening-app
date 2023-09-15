package com.ares.urlshortening.rowmapper;


import com.ares.urlshortening.domain.UserEvent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserEventRowMapper implements RowMapper<UserEvent> {
    @Override
    public UserEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserEvent.builder()
                .id(rs.getLong("id"))
                .browser(rs.getString("browser"))
                .device(rs.getString("device"))
                .description(rs.getString("description"))
                .ipAddress(rs.getString("ip_address"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .type(rs.getString("type"))
                .build();
    }
}
