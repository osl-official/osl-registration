package org.bot.service;

import org.bot.models.Setting;
import org.bot.models.entity.FreeAgent;
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
public class FreeAgentService {
    private final Setting setting;
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl = "";

    @Autowired
    public FreeAgentService(Setting setting) {
        this.setting = setting;
        baseUrl = setting.getDatabaseBaseUrl() + "/free-agent";
    }

    public List<FreeAgent> findAll() {
        ResponseEntity<FreeAgent[]> response = restTemplate.getForEntity(baseUrl, FreeAgent[].class);
        return Arrays.asList(response.getBody());
    }

    public Optional<FreeAgent> findById(long id) {
        ResponseEntity<FreeAgent> response = restTemplate.getForEntity(baseUrl + "/" + id, FreeAgent.class);
        return Optional.ofNullable(response.getBody());
    }

    public FreeAgent save(FreeAgent freeAgent) {
        PlayerService playerService = new PlayerService(setting);

        Player player = new Player();
        if (!playerService.existsById(freeAgent.getPlayer().getDiscordId())) playerService.save(player);
        return restTemplate.postForObject(baseUrl, freeAgent, FreeAgent.class);
    }

    public void remove(FreeAgent freeAgent) {
        HttpEntity<FreeAgent> request = new HttpEntity<>(freeAgent);
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
