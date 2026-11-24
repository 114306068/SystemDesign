package tw.gigradar.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tw.gigradar.dto.FollowOut;
import tw.gigradar.dto.FollowRequest;
import tw.gigradar.dto.MeOut;
import tw.gigradar.dto.NotificationOut;
import tw.gigradar.dto.PasswordChange;
import tw.gigradar.dto.ProfileUpdate;
import tw.gigradar.security.AppUserDetails;
import tw.gigradar.service.AccountService;
import tw.gigradar.service.FollowService;

/**
 * 「我的帳號」：都要先登入，而且只會看到 / 改到自己的資料
 * （使用者是誰由 Session 決定，不再從網址傳 user_id）。
 */
@RestController
@RequestMapping("/api/me")
@Tag(name = "我的帳號（需登入）")
public class MeController {

    private final AccountService accounts;
    private final FollowService follows;

    public MeController(AccountService accounts, FollowService follows) {
        this.accounts = accounts;
        this.follows = follows;
    }

    // ---------- 個人資料 ----------

    @GetMapping
    @Operation(summary = "目前登入者的資料（沒登入回 401，前端用來判斷登入狀態）")
    public MeOut me(@AuthenticationPrincipal AppUserDetails me) {
        return MeOut.from(accounts.get(me.getId()));
    }

    @PutMapping
    @Operation(summary = "修改個人資料（暱稱、電話、地區、喜好風格）")
    public MeOut updateProfile(@AuthenticationPrincipal AppUserDetails me, @Valid @RequestBody ProfileUpdate req) {
        return MeOut.from(accounts.updateProfile(me.getId(), req));
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "修改密碼（要輸入目前的密碼）")
    public void changePassword(@AuthenticationPrincipal AppUserDetails me, @Valid @RequestBody PasswordChange req) {
        accounts.changePassword(me.getId(), req);
    }

    // ---------- 2.2 我的追蹤 ----------

    @GetMapping("/follows")
    @Transactional(readOnly = true)
    public List<FollowOut> follows(@AuthenticationPrincipal AppUserDetails me) {
        return follows.list(me.getId()).stream().map(FollowOut::from).toList();
    }

    @PostMapping("/follows")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "新增追蹤：表演者 / 場館 / 音樂風格（重複追蹤回 409）")
    public FollowOut follow(@AuthenticationPrincipal AppUserDetails me, @Valid @RequestBody FollowRequest req) {
        return FollowOut.from(follows.follow(me.getId(), req));
    }

    @DeleteMapping("/follows/{followId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "取消追蹤")
    public void unfollow(@AuthenticationPrincipal AppUserDetails me, @PathVariable int followId) {
        follows.unfollow(me.getId(), followId);
    }

    // ---------- 2.3 我的通知 ----------

    @GetMapping("/notifications")
    @Transactional(readOnly = true)
    public List<NotificationOut> notifications(
            @AuthenticationPrincipal AppUserDetails me,
            @RequestParam(name = "unread_only", defaultValue = "false") boolean unreadOnly) {
        return follows.notifications(me.getId(), unreadOnly).stream().map(NotificationOut::from).toList();
    }

    @PostMapping("/notifications/{notificationId}/read")
    @Transactional
    @Operation(summary = "標示通知為已讀")
    public NotificationOut markRead(@AuthenticationPrincipal AppUserDetails me, @PathVariable int notificationId) {
        return NotificationOut.from(follows.markRead(me.getId(), notificationId));
    }
}
