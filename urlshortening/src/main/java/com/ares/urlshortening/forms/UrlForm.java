package com.ares.urlshortening.forms;

import com.ares.urlshortening.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.common.aliasing.qual.Unique;

import java.time.LocalDateTime;
@Getter
@Setter
public class UrlForm {

    @NotEmpty(message = "Real url cannot be empty")
    private String realUrl;
    @NotEmpty(message = "Short url cannot be empty")
    private String shortUrl;
    @NotEmpty(message = "Enabled cannot be empty")
    private Boolean enabled;


}
