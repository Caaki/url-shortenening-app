package com.ares.urlshortening.query;

public class UserQuery {
    public static final String COUNT_USER_EMAIL_QUERY =
            "SELECT COUNT(*) FROM Users " +
            "WHERE email = :email";

    public static final String INSERT_USER_QUERY="INSERT INTO" +
            " Users (first_name, last_name, email, password) " +
            "VALUES (:firstName, :lastName, :email, :password)";

    public static final String INSERT_VERIFICATION_URL_QUERY =
            "INSERT INTO AccountVerifications (user_id, url) " +
            "VALUES (:userId, :url) ";

    public static final String SELECT_USER_BY_EMAIL_QUERY=
            "SELECT * FROM Users " +
            "WHERE email = :email";


    public static final String INSERT_INTO_2FA_QUERY =
            "INSERT INTO TwoFactorVerifications (user_id, code) " +
            "VALUES (:userId, :code)";

    public static final String DELETE_2FA_BY_USER_ID_QUERY=
            "DELETE FROM TwoFactorVerifications " +
            "WHERE user_id = :userId";

}
