package com.ares.urlshortening.service;

import com.ares.urlshortening.domain.Url;
import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.dto.UserDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UrlService {

    Url createUrl(Url url);
    Url updateUrl(Url url);
    Page<Url> getUrls(int page,int size);
    List<Url> getUrls();
    Page<Url> getByUser(UserDTO user, int page, int size);
     List<Url> getByUser(UserDTO user);
    Url getUrl(Long id);
    void deleteUrl(Long id);
    Page<Url> searchUrls(String realUrl,Long id, int page, int size);
    Page<Url> adminSearch(String realUrl, int page, int size);
}
