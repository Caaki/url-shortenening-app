package com.ares.urlshortening.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.checkerframework.common.aliasing.qual.Unique;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"urls\"")
@Entity(name = "\"url\"")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)

public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonIgnore
    private User user;
    @NotNull
    private String realUrl;
    @Unique
    @NotNull
    private String shortUrl;
    private LocalDateTime createdAt;
    private Boolean enabled;
    private String alias;

}
