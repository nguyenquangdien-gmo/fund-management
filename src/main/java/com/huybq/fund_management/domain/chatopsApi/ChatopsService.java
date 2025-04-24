package com.huybq.fund_management.domain.chatopsApi;

import com.huybq.fund_management.domain.team.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatopsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TeamService teamService;

    @Value("${chatops.api.base-url}")
    private String baseUrl;

    private HttpHeaders createHeaders() {
        var team = teamService.getTeamBySlug("java");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(team.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public Map<String, Object> getUserByUsername(String username) {
        String url = baseUrl + "/api/v4/users/username/" + username;
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                Map.class
        );
        return response.getBody();
    }

    public Map<String, Object> getUserByEmail(String email) {
        String url = baseUrl + "/api/v4/users/email/" + email;
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                Map.class
        );
        return response.getBody();
    }

    public String getDirectChannelId(String senderId, String receiverId) {
        String url = baseUrl + "/api/v4/channels/direct";
        HttpEntity<List<String>> request = new HttpEntity<>(List.of(senderId, receiverId), createHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );
        return (String) response.getBody().get("id");
    }

    public void sendMessage(String channelId, String message) {
        String url = baseUrl + "/api/v4/posts";
        Map<String, String> body = Map.of(
                "channel_id", channelId,
                "message", message
        );
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, createHeaders());
        restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
    }

//    public static void main(String[] args) {
//        ChatopsService chatopsService = new ChatopsService();
//        String username1 = "huybq-runsystem.net";
//        Map<String, Object> user1 = chatopsService.getUserByUsername(username1);
//        String username2 = "minhnc-runsystem.net";
//        Map<String, Object> user2 = chatopsService.getUserByUsername(username2);
//        String sender = (String) user1.get("id");
//        String reciver = (String) user2.get("id");
//        String channelId = chatopsService.getDirectChannelId(sender, reciver);
//        chatopsService.sendMessage(channelId, "Hello from Java!");
//    }
}

