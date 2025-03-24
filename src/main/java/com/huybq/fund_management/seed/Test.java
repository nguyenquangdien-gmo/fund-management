package com.huybq.fund_management.seed;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

 public static List<String> extractNames(String message) {
    List<String> names = new ArrayList<>();

    // Regex để lấy tên từ cột "NAME"
    String regex = "\\|([A-ZÀ-Ỹ][^|]+?)\\s*\\|";

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(message);

    while (matcher.find()) {
        String name = matcher.group(1).trim();
        if (!name.equalsIgnoreCase("NAME")) { // Bỏ qua tiêu đề
            names.add(name);
        }
    }
    return names;
}

public static void main(String[] args) {
    String message = "@all\n:warning: THÔNG BÁO DANH SÁCH ĐI LÀM MUỘN 2025/03/24\n---\nMặc dù đã nhắc nhở nhưng vẫn còn những bạn đi làm muộn hôm nay:\n\n|NAME | CHECKIN AT|\n|--- | ---|\n|MAI ĐÌNH ĐÔNG | Nghỉ phép|\n|NGUYỄN THỊ THƠ | 08:04:16|\n|NGUYỄN THANH TÚ | 07:27:01 (Có đơn NP)|\n|LÝ TIỂU BẰNG | 08:05:20|\n|LÊ MINH TOÀN | Nghỉ phép|\n|PHẠM VŨ DUY LUÂN | Nghỉ phép|\n|NGUYỄN THANH HẢI | 08:26:25|\n|ĐỖ ĐỨC NHẬT | -|\n|VŨ TRUNG HIẾU | Nghỉ phép|\n|PHẠM HOÀNG HUY | 07:47:27 (Có đơn NP)|\n\nRất mong mọi người sẽ tuân thủ quy định và đến đúng giờ!\n\nHãy cùng nhau xây dựng môi trường làm việc chuyên nghiệp nhé  :muscle: \n\nTrân trọng!\n#checkin-statistic";

    List<String> names = extractNames(message);
    System.out.println("Danh sách người đi trễ: " + names);
}

}
