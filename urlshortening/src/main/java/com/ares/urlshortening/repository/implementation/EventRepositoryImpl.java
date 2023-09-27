package com.ares.urlshortening.repository.implementation;

import com.ares.urlshortening.domain.UrlEvent;
import com.ares.urlshortening.domain.UserEvent;
import com.ares.urlshortening.enumeration.EventType;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.repository.EventRepository;
import com.ares.urlshortening.rowmapper.UrlEventRowMapper;
import com.ares.urlshortening.rowmapper.UserEventRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

import static com.ares.urlshortening.query.EventQuery.*;

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

    @Override
    public void addVisitLinkEvent(Long userId, EventType eventType, Long urlId, String device, String browser, String ipAddress) {
        try{
            log.error(userId+ " " +eventType+ " "+urlId+ " "+ device+ " "+browser+ " "+ ipAddress );
            jdbc.update(INSERT_URL_EVENT_WITH_USER_ID_QUERY,
                    Map.of("userId",userId,"urlId",urlId,"type",eventType.toString(),"device",device,"browser",browser,"ipAddress",ipAddress));

        }catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("There was an error with the with link logic");
        }
    }

    @Override
    public Collection<UrlEvent> getUrlEventsByUserId(Long userId) {
        try{
            return jdbc.query(SELECT_URL_EVENTS_BY_USER_ID_QUERY,Map.of("userId",userId),new UrlEventRowMapper());
        }catch (EmptyResultDataAccessException e) {
            throw new ApiException("This user has no links!");
        } catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("There was an error with the urls");
        }
    }

    @Override
    public Collection<UrlEvent> getUrlEventsByUrlId(Long urlId) {
        try{
            return jdbc.query(SELECT_URL_EVENTS_BY_URL_ID_QUERY,Map.of("urlId",urlId),new UrlEventRowMapper());
        }catch (EmptyResultDataAccessException e) {
            throw new ApiException("No events on this link!");
        } catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("There was an error with the urls");
        }
    }


}
