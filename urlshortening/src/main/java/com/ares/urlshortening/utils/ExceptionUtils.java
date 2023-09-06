package com.ares.urlshortening.utils;

import com.ares.urlshortening.domain.HttpResponse;
import com.ares.urlshortening.exceptions.ApiException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ExceptionUtils {

    public static void processError(HttpServletRequest request, HttpServletResponse response, Exception ex){
        if (    ex instanceof ApiException ||
                ex instanceof DisabledException ||
                ex instanceof LockedException ||
//                ex instanceof InvalidClaimException ||
//                ex instanceof TokenExpiredException||
                ex instanceof BadCredentialsException
        ) {
            String customMessage = ex.getMessage()+ " on route: " + request.getRequestURI();
            HttpResponse httpResponse = getHttpResponse(response, customMessage, BAD_REQUEST);
            writeResponse(response, httpResponse);
        }else{
            HttpResponse httpResponse = getHttpResponse(response, "An error occurred. Please try again" ,INTERNAL_SERVER_ERROR);
            writeResponse(response, httpResponse);
        }
        log.error(ex.getMessage());
    }

    private static void writeResponse(HttpServletResponse response, HttpResponse httpResponse) {
        try{
            OutputStream out = response.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out,httpResponse);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static HttpResponse getHttpResponse(HttpServletResponse response, String message, HttpStatus httpStatus) {

        HttpResponse httpResponse = HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(message)
                .statusCode(httpStatus.value())
                .status(httpStatus)
                .build();
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());

        return httpResponse;
    }
}
