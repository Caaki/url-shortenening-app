package com.ares.urlshortening.rowmapper;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.domain.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .phone(rs.getString("phone"))
                .bio(rs.getString("bio"))
                .enabled(rs.getBoolean("enabled"))
                .isNotLocked(rs.getBoolean("not_locked"))
                .isUsingMfa(rs.getBoolean("using_mfa"))
                .imageUrl(rs.getString("image_url"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
