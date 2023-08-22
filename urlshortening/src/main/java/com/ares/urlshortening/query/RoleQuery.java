package com.ares.urlshortening.query;

public class RoleQuery {

    public static final String SELECT_ROLE_BY_NAME_QUERY=
            "SELECT * FROM Roles " +
            "WHERE name = :name";

    public static final String INSERT_ROLE_TO_USER_QUERY =
            "INSERT INTO UserRole (user_id, role_id) " +
            "VALUES (:userId, :roleId)";

    public static final String SELECT_ROLE_BY_USER_ID_QUERY=
            "SELECT r.id, r.name, r.permission FROM Roles r " +
            "JOIN UserRole ur ON ur.role_id = r.id " +
            "JOIN Users u ON u.id = ur.user_id WHERE u.id = :userId";


}
