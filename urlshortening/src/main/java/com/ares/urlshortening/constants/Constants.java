package com.ares.urlshortening.constants;

public class Constants {

    public static final String[] PUBLIC_URLS ={
            "/user/login/**",
            "/user/register/**",
            "/user/verify/code/**",
            "/user/resetpassword/**",
            "/user/verify/password/**",
            "/user/verify/account/**",
            "/user/refresh/token",
            "/user/image/**"

    };

    //Token provider constants
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String URL_SHORTENING= "Url shortening";

    public static final String MOTO = "Make urls short again!";

    public static final String AUTHORITIES = "authorities";
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 1_800_000;
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 432_000_000;


    //Filter chain prefixes
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";

    public static final String [] PUBLIC_ROUTES = {
            "/user/login",
            "/user/register",
            "/user/verify/code",
            "/user/refresh/token",
            "/user/image",
            "/user/new/password",
            "/user/refresh/token"
    };


}
