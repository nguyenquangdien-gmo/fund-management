package com.huybq.fund_management.utils.chatops;

import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Notification {
    private final TeamService teamService;

    private static final String API_URL = "https://chat.runsystem.vn/api/v4/posts";

    public void sendNotification(String messageContent, String teamSlug) {
        try {
            Team team = teamService.getTeamBySlug(teamSlug);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + team.getToken());

            Map<String, String> body = new HashMap<>();
            body.put("message", messageContent);
            body.put("channel_id", team.getChannelId());

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);

            System.out.println(response.getStatusCode() + ": " + response.getBody());
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi thông báo: " + e.getMessage());
            e.printStackTrace(); // In stack trace để dễ debug
        }
    }
    public void sendNotificationForMember(String messageContent, String userGroupIdm, String teamSlug) {
        try {
            Team team = teamService.getTeamBySlug(teamSlug);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + team.getToken());

            Map<String, String> body = new HashMap<>();
            body.put("message", messageContent);
            body.put("channel_id", team.getChannelId());

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);

            System.out.println(response.getStatusCode() + ": " + response.getBody());
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi thông báo: " + e.getMessage());
            e.printStackTrace(); // In stack trace để dễ debug
        }
    }


    public static void main(String[] args) {
//        Notification notification = new Notification("java");
//        notification.sendNotification("@all\n:warning: THÔNG BÁO DANH SÁCH ĐI LÀM MUỘN 2025/03/24\n---\nMặc dù đã nhắc nhở nhưng vẫn còn những bạn đi làm muộn hôm nay:\n\n|NAME | CHECKIN AT|\n|--- | ---|\n|MAI ĐÌNH ĐÔNG | Nghỉ phép|\n|NGUYỄN THỊ THƠ | 08:04:16|\n|NGUYỄN THANH TÚ | 07:27:01 (Có đơn NP)|\n|LÝ TIỂU BẰNG | 08:05:20|\n|LÊ MINH TOÀN | Nghỉ phép|\n|PHẠM VŨ DUY LUÂN | Nghỉ phép|\n|NGUYỄN THANH HẢI | 08:26:25|\n|ĐỖ ĐỨC NHẬT | -|\n|VŨ TRUNG HIẾU | Nghỉ phép|\n|PHẠM HOÀNG HUY | 07:47:27 (Có đơn NP)|\n\nRất mong mọi người sẽ tuân thủ quy định và đến đúng giờ!\n\nHãy cùng nhau xây dựng môi trường làm việc chuyên nghiệp nhé  :muscle: \n\nTrân trọng!\n#checkin-statistic\n\n![image](https://drive.google.com/uc?export=download&id=1Rj3_T_EoxvsBx-6gwAcfxOdSF8AUg6ui)");
    }
}
