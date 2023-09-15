package com.ares.urlshortening.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserEvent {
    private Long id;
    private String type;
    private String description;
    private String device;
    private String browser;
    private String ipAddress;
    private LocalDateTime createdAt;
}
