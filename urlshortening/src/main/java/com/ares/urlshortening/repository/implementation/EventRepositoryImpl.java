package com.ares.urlshortening.repository.implementation;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.domain.UserEvent;
import com.ares.urlshortening.enumeration.EventType;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.repository.EventRepository;
import com.ares.urlshortening.rowmapper.RoleRowMapper;
import com.ares.urlshortening.rowmapper.UserEventRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

import static com.ares.urlshortening.enumeration.RoleType.ROLE_USER;
import static com.ares.urlshortening.query.EventQuery.*;
import static com.ares.urlshortening.query.RoleQuery.SELECT_ROLE_BY_USER_ID_QUERY;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventRepositoryImpl implements EventRepository {
    private final NamedParameterJdbcTemplate jdbc;
    @Override
    public Collection<UserEvent> getEventsByUserId(Long userId) {
        try{
            return jdbc.query(SELECT_EVENTS_BY_USER_ID_QUERY, Map.of("userId",userId),new UserEventRowMapper());
        }catch (EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("No user with the given id");
        }catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }
    @Override
    public void addUserEvent(Long id, EventType eventType, String device, String browser, String ipAddress) {

        try{
            jdbc.update(INSERT_USER_EVENT_WITH_USER_ID_QUERY,
                    Map.of("userId",id,"type",eventType.toString(),"device",device,"browser",browser,"ipAddress",ipAddress)
            );
        }catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }

    }
    @Override
    public void addUserEvent(String email, EventType eventType, String device, String browser, String ipAddress) {
        try{
            jdbc.update(INSERT_USER_EVENT_WITH_USER_EMAIL_QUERY,
                    Map.of("email",email,"type",eventType,"device",device,"browser",browser,"ipAddress",ipAddress)
            );
        }catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }
}
