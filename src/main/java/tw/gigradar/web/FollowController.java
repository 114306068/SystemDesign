package tw.gigradar.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tw.gigradar.dto.NotificationOut;
import tw.gigradar.repository.NotificationRepository;

/**
 * 2.0 查詢與追蹤演出 —— 2.2 設定追蹤、2.3 新演出通知。
 *
 * <p>已完成：查看通知。
 * <p>待實作：
 * <ul>
 *   <li>2.2 POST /api/users/{id}/follows     新增追蹤（表演者 / 場館 / 風格），程式檢查重複</li>
 *   <li>DELETE /api/follows/{id}             取消追蹤</li>
 *   <li>POST /api/notifications/{id}/read</li>
 * </ul>
 * （登入機制做好後，{id} 改成從登入狀態取得目前使用者）
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "2.0 追蹤與通知")
@Transactional(readOnly = true)
public class FollowController {

    private final NotificationRepository notifications;

    public FollowController(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @GetMapping("/{userId}/notifications")
    public List<NotificationOut> notifications(
            @PathVariable int userId,
            @RequestParam(name = "unread_only", defaultValue = "false") boolean unreadOnly) {
        var list = unreadOnly
                ? notifications.findByUserIdAndReadFalseOrderByCreatedAtDescIdDesc(userId)
                : notifications.findByUserIdOrderByCreatedAtDescIdDesc(userId);
        return list.stream().map(NotificationOut::from).toList();
    }
}
