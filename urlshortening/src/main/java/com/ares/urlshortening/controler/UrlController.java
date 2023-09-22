package com.ares.urlshortening.controler;

import com.ares.urlshortening.domain.HttpResponse;
import com.ares.urlshortening.domain.Url;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.dto.dtomapper.UserDTOMapper;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.forms.UrlForm;
import com.ares.urlshortening.service.UrlService;
import com.ares.urlshortening.utils.UpdateUtils;
import com.zaxxer.hikari.util.PropertyElf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static com.ares.urlshortening.utils.UpdateUtils.*;
import static java.time.LocalDateTime.now;

@Slf4j
@RestController
@RequestMapping(path = "/url")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @GetMapping("/list")
    public ResponseEntity<HttpResponse> getUrls(
            @AuthenticationPrincipal UserDTO authentication,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size) {
        return ResponseEntity.ok((
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Urls retrieved")
                        .data(Map.of("user", authentication, "urls", urlService.getByUser(authentication, page.orElse(0), size.orElse(10))))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        ));
    }

    @PostMapping("/create")
    public ResponseEntity<HttpResponse> createUrl(
            @AuthenticationPrincipal UserDTO authentication, @RequestBody UrlForm url) {

        Url newUrl =  Url.builder()
                .realUrl(url.getRealUrl())
                .shortUrl(url.getShortUrl())
                .enabled(url.getEnabled())
                .user(UserDTOMapper.fromDTO(authentication))
                .createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.created(URI.create(""))
                .body(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
                                .message("Url created")
                                .data(Map.of("user", authentication, "urls", urlService.createUrl(newUrl)))
                                .status(HttpStatus.CREATED)
                                .statusCode(HttpStatus.CREATED.value())
                                .build()
                );
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<HttpResponse> getUrl(
            @AuthenticationPrincipal UserDTO authentication,
            @PathVariable("id") Long id) {
        Url url = urlService.getUrl(id);
        if (Objects.equals(authentication.getId(), url.getUser().getId()) || authentication.getRoleName().equals("ROLE_SYSADMIN")) {
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Urls retrieved")
                            .data(Map.of("user", authentication, "url", url))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            ));
        } else {
            throw new ApiException("This url doesnt belong to you, login as the owner to make adjustments!");
        }
    }


    @GetMapping("/search")
    public ResponseEntity<HttpResponse> searchUrls(
            @AuthenticationPrincipal UserDTO authentication,
            @RequestParam Optional<String> url,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size) {

        if (authentication.getRoleName().equals("ROLE_SYSADMIN")) {
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Urls retrieved")
                            .data(Map.of("user", authentication, "urls",
                                    urlService.adminSearch(url.orElse(""), page.orElse(0), size.orElse(10))))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            ));
        } else {
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Urls retrieved")
                            .data(Map.of("user", authentication, "urls",
                                    urlService.searchUrls(url.orElse(""), authentication.getId(), page.orElse(0), size.orElse(10))))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            ));
        }
    }


    @PatchMapping(path = "/update")
    public ResponseEntity<HttpResponse> update(
            @AuthenticationPrincipal UserDTO authentication,
            @RequestBody Url url) {

        Url existing = urlService.getUrl(url.getId());
        copyNonNullProperties(url, existing);
        if (Objects.equals(authentication.getId(), existing.getUser().getId())){
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Urls retrieved")
                            .data(Map.of("user", authentication, "url",
                                    urlService.updateUrl(existing)))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            ));
        }else {
            throw new ApiException("You are not authorized to make this change!");
        }

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpResponse> deleteUrl(
            @AuthenticationPrincipal UserDTO authentication,
            @PathVariable("id") Long id
    ) {
        Url forDeletion = urlService.getUrl(id);
        log.error(forDeletion.getUser().getId().toString() + " Url ID");
        log.error(authentication.getId() + " Authentification id ");
        log.error(authentication.getPermissions());
        if (Objects.equals(forDeletion.getUser().getId(), authentication.getId()) || authentication.getRoleName().equals("ROLE_SYSADMIN")) {
            urlService.deleteUrl(id);
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Url deleted")
                            .data(Map.of("user", authentication))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            ));
        } else {
            throw new ApiException("You are not authorized to make this change!");
        }
    }


}
