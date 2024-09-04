package org.bot.service;

import org.bot.models.Setting;
import org.bot.models.entity.Player;
import org.bot.models.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    private final Setting setting;
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl = "";

    @Autowired
    public TeamService(Setting setting) {
        this.setting = setting;
        baseUrl = setting.getDatabaseBaseUrl() + "/team-player";
    }

    public List<Team> findAll() {
        ResponseEntity<Team[]> response = restTemplate.getForEntity(baseUrl, Team[].class);
        return Arrays.asList(response.getBody());
    }

    public Optional<Team> findById(String id) {
        ResponseEntity<Team> response = restTemplate.getForEntity(baseUrl + "/" + id, Team.class);
        return Optional.ofNullable(response.getBody());
    }

    public List<Team> findAllByLeagueIsNull() {
        ResponseEntity<Team[]> response = restTemplate.getForEntity(baseUrl + "/empty", Team[].class);
        return Arrays.asList(response.getBody());
    }

    public List<Team> findAllByLeagueIsNotNull() {
        ResponseEntity<Team[]> response = restTemplate.getForEntity(baseUrl + "/taken", Team[].class);
        return Arrays.asList(response.getBody());
    }

    public Team save(Team team) {
        return restTemplate.postForObject(baseUrl, team, Team.class);
    }

    public void remove(Team team) {
        HttpEntity<Team> request = new HttpEntity<>(team);
        restTemplate.exchange(baseUrl, HttpMethod.DELETE, request, Void.class);
    }

    public void removeById(String id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public boolean existsById(String id) {
        ResponseEntity<Boolean> response = restTemplate.getForEntity(baseUrl + "/exists/" + id, Boolean.class);
        return response.getBody();
    }
}
