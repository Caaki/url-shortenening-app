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

    public static final String SELECT_USER_BY_ID_QUERY =
            "SELECT * FROM Users " +
            "WHERE id = :userId";


    public static final String SELECT_USER_BY_USER_CODE_QUERY =
            "SELECT * FROM Users " +
            "WHERE id = (SELECT user_id FROM TwoFactorVerifications WHERE code = :code)";

    public static final String IS_VERIFICATION_CODE_EXPIRED_BY_CODE =
            "SELECT expiration_date < NOW() " +
            "FROM TwoFactorVerifications " +
            "WHERE code = :code";



    public static final String DELETE_RESETPASSWORD_BY_USER_ID_QUERY=
            "DELETE FROM ResetPasswordVerifications " +
            "WHERE user_id = :userId";

    public static final String INSERT_INTO_RESETPASSWORD_QUERY =
            "INSERT INTO ResetPasswordVerifications(user_id,url) " +
            "VALUES(:userId,:url)";

    public static final String IS_PASSWORD_RESET_CODE_EXPIRED_BY_URL =
            "SELECT expiration_date < NOW() " +
            "FROM ResetPasswordVerifications "+
            "WHERE url = :url";

    public static final String SELECT_USER_BY_PASSWORD_URL_QUERY=
            "SELECT * FROM Users "+
            "WHERE id = (" +
                    "SELECT user_id " +
                    "FROM ResetPasswordVerifications " +
                    "WHERE url = :url" +
                    ")";

    public static final String UPDATE_USER_PASSWORD_BY_URL_QUERY=
            "UPDATE Users SET password = :newPassword " +
            "WHERE id = (" +
                    "SELECT user_id FROM ResetPasswordVerifications " +
                    "WHERE url = :url)";

    public static final String DELETE_RESETPASSWORD_BY_URL_QUERY =
            "DELETE FROM ResetPasswordVerifications " +
            "WHERE url = :url";


    public static final String SELECT_USER_BY_VERIFY_URL_QUERY =
            "SELECT * FROM Users " +
            "WHERE id = (" +
                    "SELECT user_id FROM AccountVerifications " +
                    "WHERE url = :url)";

    public static final String UPDATE_USER_ENABLE_ACCOUNT_QUERY=
            "UPDATE Users SET enabled = :enabled " +
            "WHERE id = :userId";


}
