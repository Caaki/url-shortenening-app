package com.ares.urlshortening.forms;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UrlForm {

    @NotEmpty(message = "Real url cannot be empty")
    private String realUrl;
    @NotEmpty(message = "Short url cannot be empty")
    private String shortUrl;
    private Boolean enabled;
    @NotEmpty(message = "Alias cannot be empty")
    private String alias;

}
