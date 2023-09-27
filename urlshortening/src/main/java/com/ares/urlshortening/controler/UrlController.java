package com.ares.urlshortening.controler;

import com.ares.urlshortening.domain.HttpResponse;
import com.ares.urlshortening.domain.Url;
import com.ares.urlshortening.domain.UrlEvent;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.dto.dtomapper.UserDTOMapper;
import com.ares.urlshortening.event.NewUserEvent;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.forms.UrlForm;
import com.ares.urlshortening.service.EventService;
import com.ares.urlshortening.service.UrlService;
import com.ares.urlshortening.utils.UpdateUtils;
import com.zaxxer.hikari.util.PropertyElf;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ares.urlshortening.enumeration.EventType.MFA_UPDATE;
import static com.ares.urlshortening.enumeration.EventType.URL_VISITED;
import static com.ares.urlshortening.utils.UpdateUtils.*;
import static java.time.LocalDateTime.now;

@Slf4j
@RestController
@RequestMapping(path = "/url")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final EventService eventService;
    private final ApplicationEventPublisher publisher;

    @GetMapping("/list")
    public ResponseEntity<HttpResponse> getUrls(
            @AuthenticationPrincipal UserDTO authentication,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size) {
        return ResponseEntity.ok((
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Urls retrieved")
                        .data(Map.of("user", authentication, "page", urlService.getByUser(authentication, page.orElse(0), size.orElse(10))))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        ));
    }

    @PostMapping("/create")
    public ResponseEntity<HttpResponse> createUrl(
            @AuthenticationPrincipal UserDTO authentication, @RequestBody @Valid UrlForm url) {
        Url newUrl =  Url.builder()
                .realUrl(url.getRealUrl())
                .shortUrl(url.getShortUrl())
                .enabled(url.getEnabled())
                .user(UserDTOMapper.fromDTO(authentication))
                .createdAt(LocalDateTime.now())
                .alias(url.getAlias())
                .build();
        return ResponseEntity.created(URI.create(""))
                .body(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
                                .message("Url created")
                                .data(Map.of("user", authentication, "url", urlService.createUrl(newUrl)))
                                .status(HttpStatus.CREATED)
                                .statusCode(HttpStatus.CREATED.value())
                                .build()
                );
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<HttpResponse> getUrl(
            @AuthenticationPrincipal UserDTO authentication,
            @PathVariable("id") Long id) {

        Collection<UrlEvent> events = eventService.getUrlEventsByUrlId(id);
        Url url = urlService.getUrl(id);
        if (Objects.equals(authentication.getId(), url.getUser().getId()) || authentication.getRoleName().equals("ROLE_SYSADMIN")) {
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Urls retrieved")
                            .data(Map.of("user", authentication, "url", url,"urlEvents", events))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            ));

        } else {
            throw new ApiException("This url doesnt belong to you, login as the owner to make adjustments!");
        }
    }

    @GetMapping("/redirect/{link}")
    public ResponseEntity<HttpResponse> redirectLink(@PathVariable("link") String link) throws InterruptedException {
        Url url = urlService.redirectUrl(link);
        log.error("url.toString()");
        if (url == null){
            throw new ApiException("Invalid Url!");
        }else{
            publisher.publishEvent(new NewUserEvent(URL_VISITED, url.getUser().getId(),url.getId()));
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Urls retrieved")
                            .data(Map.of("url",url.getRealUrl()))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            ));
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
        Collection<UrlEvent> events = eventService.getUrlEventsByUrlId(url.getId());
        log.error(url.getShortUrl());
        Url existing = urlService.getUrl(url.getId());
        copyNonNullProperties(url, existing);
        if (Objects.equals(authentication.getId(), existing.getUser().getId())){
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Urls retrieved")
                            .data(Map.of("user", authentication, "url",
                                    urlService.updateUrl(existing),"urlEvents", events))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            ));
        }else {
            throw new ApiException("You are not authorized to make this change!");
        }

    }

    @GetMapping("/visits")
    public ResponseEntity<HttpResponse> userVisits(@AuthenticationPrincipal UserDTO auth){

        Collection<UrlEvent> events = eventService.getUrlEventsByUserId(auth.getId());
        return ResponseEntity.ok((
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Urls retrieved")
                        .data(Map.of("urlEvents", events))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
                ));
    }

    @GetMapping("/visits/{id}")
    public ResponseEntity<HttpResponse> urlVisitsByUrlId(@AuthenticationPrincipal UserDTO auth,  @PathVariable("id") Long id){

        Url url = urlService.getUrl(id);
        if (Objects.equals(url.getUser().getId(), auth.getId()) || auth.getRoleName().equals("ROLE_SYSADMIN")){
            return ResponseEntity.ok((
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Url Events retrieved")
                            .data(Map.of("user", auth,"urlEvents",eventService.getUrlEventsByUrlId(id)))
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
                    ) );
        }else{
            throw new ApiException("You are not authorized to view this content!");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpResponse> deleteUrl(
            @AuthenticationPrincipal UserDTO authentication,
            @PathVariable("id") Long id
    ) {
        Url forDeletion = urlService.getUrl(id);
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
