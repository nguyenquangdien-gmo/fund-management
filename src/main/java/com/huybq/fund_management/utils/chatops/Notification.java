package com.huybq.fund_management.utils.chatops;

import com.huybq.fund_management.domain.chatopsApi.ChatopsService;
import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.team.TeamService;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Notification {
    private final TeamService teamService;
    private final ChatopsService chatopsService;
    private final UserRepository userRepository;

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

    public void sendNotificationForMember(String messageContent, String senderEmail, String receiverEmail) {
        try {
            // Lấy thông tin user nhận bằng email
            Map<String, Object> receiver = chatopsService.getUserByEmail(receiverEmail);
            String receiverId = (String) receiver.get("id");

            Map<String, Object> sender = chatopsService.getUserByEmail(senderEmail);
            String senderId = (String) sender.get("id");

            // Tạo channel direct giữa sender và receiver
            String channelId = chatopsService.getDirectChannelId(receiverId, senderId);

            // Gửi tin nhắn
            chatopsService.sendMessage(channelId, messageContent);

        } catch (Exception e) {
            System.err.println("Failed to send message to " + receiverEmail + ": " + e.getMessage());
        }
    }

    public void sendBirthdayAndAnniversaryNotifications() {
        LocalDate today = LocalDate.now();

        List<User> users = userRepository.findAllByIsDeleteIsFalse();

        for (User user : users) {
            if (user.getDob() != null &&
                    user.getDob().getMonth() == today.getMonth() &&
                    user.getDob().getDayOfMonth() == today.getDayOfMonth()) {
                String message = "@all\n🎂 Hôm nay là sinh nhật của " + user.getFullName() + " 🎉\n"
                        +"Chúng ta hãy cùng gửi những lời chúc sinh nhật thật nhiều ý nghĩa tới những người đồng nghiệp của mình nhé!\n"
                        + ":emo_flower: Chúc bạn một ngày sinh nhật vui vẻ và thật nhiều niềm vui!";
                sendNotification( message,"java");
            }

            // thông báo kỷ niệm gia nhập
            if (user.getJoinDate() != null &&
                    user.getJoinDate().getMonth() == today.getMonth() &&
                    user.getJoinDate().getDayOfMonth() == today.getDayOfMonth()) {
                int years = today.getYear() - user.getJoinDate().getYear();
                String message = "@all\n🎉 Hôm nay là kỷ niệm " + years + " năm " + user.getFullName()
                        + " gia nhập team!\nCảm ơn bạn đã đồng hành cùng tập thể team và công ty nhé!❤️";
                sendNotification(message, "java");
            }
        }
    }

    public static void main(String[] args) {
//        Notification notification = new Notification("java");
//        notification.sendNotification("@all\n:warning: THÔNG BÁO DANH SÁCH ĐI LÀM MUỘN 2025/03/24\n---\nMặc dù đã nhắc nhở nhưng vẫn còn những bạn đi làm muộn hôm nay:\n\n|NAME | CHECKIN AT|\n|--- | ---|\n|MAI ĐÌNH ĐÔNG | Nghỉ phép|\n|NGUYỄN THỊ THƠ | 08:04:16|\n|NGUYỄN THANH TÚ | 07:27:01 (Có đơn NP)|\n|LÝ TIỂU BẰNG | 08:05:20|\n|LÊ MINH TOÀN | Nghỉ phép|\n|PHẠM VŨ DUY LUÂN | Nghỉ phép|\n|NGUYỄN THANH HẢI | 08:26:25|\n|ĐỖ ĐỨC NHẬT | -|\n|VŨ TRUNG HIẾU | Nghỉ phép|\n|PHẠM HOÀNG HUY | 07:47:27 (Có đơn NP)|\n\nRất mong mọi người sẽ tuân thủ quy định và đến đúng giờ!\n\nHãy cùng nhau xây dựng môi trường làm việc chuyên nghiệp nhé  :muscle: \n\nTrân trọng!\n#checkin-statistic\n\n![image](https://drive.google.com/uc?export=download&id=1Rj3_T_EoxvsBx-6gwAcfxOdSF8AUg6ui)");
    }
}
