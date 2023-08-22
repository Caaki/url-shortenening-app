package com.ares.urlshortening.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class HttpResponse {

    protected String timeStamp;
    protected int statusCode;
    protected HttpStatusCode status;
    protected String reason;
    protected String message;
    protected String developerMessage;
    protected Map<?,?> data;

}
