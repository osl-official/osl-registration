package org.bot.service;

import org.bot.models.Setting;
import org.bot.models.entity.TeamPlayer;
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
public class TeamPlayerService {
    private final Setting setting;
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl = "";

    @Autowired
    public TeamPlayerService(Setting setting) {
        this.setting = setting;
        baseUrl = setting.getDatabaseBaseUrl() + "/team-player";
    }

    public List<TeamPlayer> findAll() {
        ResponseEntity<TeamPlayer[]> response = restTemplate.getForEntity(baseUrl, TeamPlayer[].class);
        return Arrays.asList(response.getBody());
    }

    public Optional<List<TeamPlayer>> findAllByTeamId(String id) {
        ResponseEntity<TeamPlayer[]> response = restTemplate.getForEntity(baseUrl + "/team/" + id, TeamPlayer[].class);
        return Optional.of(Arrays.asList(response.getBody()));
    }

    public Optional<TeamPlayer> findById(long id) {
        ResponseEntity<TeamPlayer> response = restTemplate.getForEntity(baseUrl + "/" + id, TeamPlayer.class);
        return Optional.ofNullable(response.getBody());
    }

    public TeamPlayer save(TeamPlayer teamPlayer) {
        return restTemplate.postForObject(baseUrl, teamPlayer, TeamPlayer.class);
    }

    public void remove(TeamPlayer teamPlayer) {
        HttpEntity<TeamPlayer> request = new HttpEntity<>(teamPlayer);
        restTemplate.exchange(baseUrl, HttpMethod.DELETE, request, Void.class);
    }

    public void removeById(long id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public boolean existsById(long id) {
        ResponseEntity<Boolean> response = restTemplate.getForEntity(baseUrl + "/exists/" + id, Boolean.class);
        return response.getBody();
    }
}
