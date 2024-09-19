package org.bot.service;

import org.bot.models.Setting;
import org.bot.models.entity.Player;
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
public class PlayerService {
    private final Setting setting;
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl = "";

    @Autowired
    public PlayerService(Setting setting) {
        this.setting = setting;
        baseUrl = setting.getDatabaseBaseUrl() + "/player";
    }

    public List<Player> findAll() {
        ResponseEntity<Player[]> response = restTemplate.getForEntity(baseUrl, Player[].class);
        return Arrays.asList(response.getBody());
    }

    public Optional<Player> findById(long id) {
        ResponseEntity<Player> response = restTemplate.getForEntity(baseUrl + "/" + id, Player.class);
        return Optional.ofNullable(response.getBody());
    }

    public Player save(Player player) {
        return restTemplate.postForObject(baseUrl, player, Player.class);
    }

    public void remove(Player player) {
        HttpEntity<Player> request = new HttpEntity<>(player);
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
