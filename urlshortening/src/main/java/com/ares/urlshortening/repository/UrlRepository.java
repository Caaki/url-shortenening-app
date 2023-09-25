package com.ares.urlshortening.repository;

import com.ares.urlshortening.domain.Url;
import com.ares.urlshortening.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UrlRepository
        extends PagingAndSortingRepository<Url,Long>,
        ListCrudRepository<Url,Long> {

    Page<Url> findByRealUrlContainingAndUser_Id(String realUrl,Long userId, Pageable pageable);
    Page<Url> findAllByUser(User user, Pageable pageable);
    List<Url> findAllByUser(User user);
    Page<Url> findByRealUrlContaining(String realUrl, Pageable pageable);
    Url findByShortUrl(String shortUrl);

}
