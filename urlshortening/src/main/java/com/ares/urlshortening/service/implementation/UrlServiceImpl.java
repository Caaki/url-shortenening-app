package com.ares.urlshortening.service.implementation;

import com.ares.urlshortening.domain.Url;
import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.dto.dtomapper.UserDTOMapper;
import com.ares.urlshortening.repository.UrlRepository;
import com.ares.urlshortening.service.UrlService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.domain.PageRequest.of;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    @Override
    public Url createUrl(Url url) {
        return urlRepository.save(url);
    }

    @Override
    public Url updateUrl(Url url) {
        return urlRepository.save(url);
    }

    @Override
    public Page<Url> getUrls(int page, int size) {
        return urlRepository.findAll(of(page,size));
    }

    @Override
    public List<Url> getUrls() {
        return urlRepository.findAll();
    }

    @Override
    public Page<Url> getByUser(UserDTO user, int page, int size) {
        return urlRepository.findAllByUser(UserDTOMapper.fromDTO(user),of(page,size));
    }
    @Override
    public List<Url> getByUser(UserDTO user) {
        return urlRepository.findAllByUser(UserDTOMapper.fromDTO(user));
    }

    @Override
    public Url getUrl(Long id) {
        return urlRepository.findById(id).get();
    }

    @Override
    public void deleteUrl(Long id) {
        urlRepository.deleteById(id);
    }

    @Override
    public Page<Url> searchUrls(String realUrl,Long id, int page, int size) {
        return urlRepository.findByRealUrlContainingAndUser_Id(realUrl,id, of(page,size));
    }

    @Override
    public Page<Url> adminSearch(String realUrl, int page, int size) {
        return urlRepository.findByRealUrlContaining(realUrl,of(page,size));
    }
}
