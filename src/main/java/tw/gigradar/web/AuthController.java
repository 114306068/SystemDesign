package tw.gigradar.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tw.gigradar.dto.LoginRequest;
import tw.gigradar.dto.MeOut;
import tw.gigradar.dto.RegisterRequest;
import tw.gigradar.model.User;
import tw.gigradar.security.AppUserDetails;
import tw.gigradar.security.AppUserDetailsService;
import tw.gigradar.service.AccountService;

/**
 * 註冊與登入。登出是 POST /api/auth/logout（由 SecurityConfig 處理，回 204）。
 * 登入成功後，登入狀態存在 Session，之後的請求瀏覽器會自動帶上 JSESSIONID cookie。
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "會員登入")
@ConditionalOnWebApplication
public class AuthController {

    private final AuthenticationManager authManager;
    private final SecurityContextRepository contextRepository;
    private final AccountService accounts;

    public AuthController(AuthenticationManager authManager, SecurityContextRepository contextRepository,
                          AccountService accounts) {
        this.authManager = authManager;
        this.contextRepository = contextRepository;
        this.accounts = accounts;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "註冊（成功後直接登入）")
    public MeOut register(@Valid @RequestBody RegisterRequest req, HttpServletRequest request, HttpServletResponse response) {
        User user = accounts.register(req);
        signIn(user.getEmail(), req.password(), request, response);
        return MeOut.from(user);
    }

    @PostMapping("/login")
    @Operation(summary = "登入")
    public MeOut login(@Valid @RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        AppUserDetails me = signIn(AppUserDetailsService.normalizeEmail(req.email()), req.password(), request, response);
        return MeOut.from(accounts.get(me.getId()));
    }

    /** 驗證帳密，成功就把登入狀態存進 Session。 */
    private AppUserDetails signIn(String email, String password, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth;
        try {
            auth = authManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        } catch (AuthenticationException e) {
            // 不說是帳號還是密碼錯，避免別人用來猜哪些 Email 有註冊
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email 或密碼錯誤");
        }
        if (request.getSession(false) != null) {
            request.changeSessionId(); // 登入後換新的 Session ID，防止 Session 固定攻擊
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return (AppUserDetails) auth.getPrincipal();
    }
}
