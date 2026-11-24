package tw.gigradar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tw.gigradar.model.Event;
import tw.gigradar.model.Notification;
import tw.gigradar.model.User;
import tw.gigradar.repository.EventRepository;
import tw.gigradar.repository.NotificationRepository;
import tw.gigradar.repository.UserRepository;
import tw.gigradar.task.DemoData;

/** 登入、註冊、個人資料、我的追蹤與通知。走真正的登入 API，用 Session 帶著登入狀態。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired DemoData demoData;
    @Autowired UserRepository users;
    @Autowired EventRepository events;
    @Autowired NotificationRepository notifications;

    @BeforeEach
    void seed() {
        demoData.seed();
    }

    // ---------- 小工具 ----------

    private MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder req, Object body) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)).with(csrf());
    }

    private ResultActions call(MockHttpServletRequestBuilder req, MockHttpSession session) throws Exception {
        return mvc.perform(session == null ? req : req.session(session));
    }

    private JsonNode body(ResultActions result) throws Exception {
        String s = result.andReturn().getResponse().getContentAsString();
        return s.isEmpty() ? null : json.readTree(s);
    }

    /** 登入並回傳帶著登入狀態的 Session。 */
    private MockHttpSession login(String email, String password) throws Exception {
        ResultActions r = mvc.perform(withJson(post("/api/auth/login"), Map.of("email", email, "password", password)))
                .andExpect(status().isOk());
        return (MockHttpSession) r.andReturn().getRequest().getSession(false);
    }

    private MockHttpSession loginAudience() throws Exception {
        return login("audience@demo.tw", DemoData.PASSWORD);
    }

    // ---------- 登入 / 註冊 ----------

    @Test
    void loginWithDemoAccount() throws Exception {
        MockHttpSession session = login("Audience@Demo.tw ", DemoData.PASSWORD); // 不分大小寫、會去空白
        JsonNode me = body(call(get("/api/me"), session).andExpect(status().isOk()));
        assertThat(me.get("email").asText()).isEqualTo("audience@demo.tw");
        assertThat(me.get("role").asText()).isEqualTo("audience");
        assertThat(me.get("district").asText()).isEqualTo("大安區");
        assertThat(me.has("password_hash")).isFalse();
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        JsonNode err = body(mvc.perform(withJson(post("/api/auth/login"),
                Map.of("email", "audience@demo.tw", "password", "wrong-password"))).andExpect(status().isUnauthorized()));
        assertThat(err.get("detail").asText()).isEqualTo("Email 或密碼錯誤");
        // 不存在的帳號回一樣的訊息
        mvc.perform(withJson(post("/api/auth/login"), Map.of("email", "nobody@demo.tw", "password", "whatever1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meRequiresLogin() throws Exception {
        JsonNode err = body(mvc.perform(get("/api/me")).andExpect(status().isUnauthorized()));
        assertThat(err.get("detail").asText()).isEqualTo("請先登入");
    }

    @Test
    void postWithoutCsrfTokenIsBlocked() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"audience@demo.tw\",\"password\":\"demo1234\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerThenLoggedIn() throws Exception {
        ResultActions r = mvc.perform(withJson(post("/api/auth/register"), Map.of(
                "email", "New.Fan@Example.com", "password", "secret123", "display_name", " 新樂迷 ", "role", "audience")))
                .andExpect(status().isCreated());
        MockHttpSession session = (MockHttpSession) r.andReturn().getRequest().getSession(false);

        JsonNode me = body(call(get("/api/me"), session).andExpect(status().isOk()));
        assertThat(me.get("email").asText()).isEqualTo("new.fan@example.com");
        assertThat(me.get("display_name").asText()).isEqualTo("新樂迷");

        User saved = users.findByEmail("new.fan@example.com").orElseThrow();
        assertThat(saved.getPasswordHash()).startsWith("pbkdf2_sha256$").doesNotContain("secret123");
    }

    @Test
    void registerRejectsBadInput() throws Exception {
        JsonNode dup = body(mvc.perform(withJson(post("/api/auth/register"), Map.of(
                "email", "audience@demo.tw", "password", "secret123", "display_name", "x", "role", "audience")))
                .andExpect(status().isConflict()));
        assertThat(dup.get("detail").asText()).isEqualTo("這個 Email 已經註冊過");

        JsonNode shortPw = body(mvc.perform(withJson(post("/api/auth/register"), Map.of(
                "email", "a@b.tw", "password", "123", "display_name", "x", "role", "audience")))
                .andExpect(status().isBadRequest()));
        assertThat(shortPw.get("detail").asText()).isEqualTo("密碼至少 8 個字元");

        mvc.perform(withJson(post("/api/auth/register"), Map.of(
                "email", "boss@b.tw", "password", "secret123", "display_name", "x", "role", "admin")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutEndsSession() throws Exception {
        MockHttpSession session = loginAudience();
        call(post("/api/auth/logout").with(csrf()), session).andExpect(status().isNoContent());
        mvc.perform(get("/api/me").session(session)).andExpect(status().isUnauthorized());
    }

    // ---------- 個人資料 ----------

    @Test
    void updateProfile() throws Exception {
        MockHttpSession session = loginAudience();
        JsonNode me = body(call(withJson(put("/api/me"), Map.of(
                "display_name", "改名後", "phone", "", "district", "信義區", "favorite_genre", "jazz")), session)
                .andExpect(status().isOk()));
        assertThat(me.get("display_name").asText()).isEqualTo("改名後");
        assertThat(me.get("phone").isNull()).isTrue(); // 空字串 = 清除
        assertThat(me.get("district").asText()).isEqualTo("信義區");

        call(withJson(put("/api/me"), Map.of("display_name", "x", "phone", "abc")), session)
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword() throws Exception {
        MockHttpSession session = loginAudience();
        JsonNode err = body(call(withJson(put("/api/me/password"),
                Map.of("current_password", "wrong", "new_password", "newpass123")), session)
                .andExpect(status().isBadRequest()));
        assertThat(err.get("detail").asText()).isEqualTo("目前的密碼不正確");

        call(withJson(put("/api/me/password"),
                Map.of("current_password", DemoData.PASSWORD, "new_password", "newpass123")), session)
                .andExpect(status().isNoContent());
        login("audience@demo.tw", "newpass123"); // 新密碼可以登入
        mvc.perform(withJson(post("/api/auth/login"), Map.of("email", "audience@demo.tw", "password", DemoData.PASSWORD)))
                .andExpect(status().isUnauthorized()); // 舊密碼不行
    }

    // ---------- 我的追蹤 ----------

    @Test
    void followAndUnfollow() throws Exception {
        MockHttpSession session = loginAudience();
        // 示範資料裡觀眾已追蹤 霧島樂隊 與 jazz
        assertThat(body(call(get("/api/me/follows"), session).andExpect(status().isOk()))).hasSize(2);

        int venueId = events.findAll().get(0).getVenue().getId();
        JsonNode created = body(call(withJson(post("/api/me/follows"),
                Map.of("target_type", "venue", "venue_id", venueId)), session).andExpect(status().isCreated()));
        assertThat(created.at("/venue/id").asInt()).isEqualTo(venueId);

        call(withJson(post("/api/me/follows"), Map.of("target_type", "venue", "venue_id", venueId)), session)
                .andExpect(status().isConflict()); // 重複追蹤
        call(withJson(post("/api/me/follows"), Map.of("target_type", "genre", "genre", "jazz")), session)
                .andExpect(status().isConflict());
        call(withJson(post("/api/me/follows"), Map.of("target_type", "performer", "performer_id", 999999)), session)
                .andExpect(status().isNotFound());

        call(delete("/api/me/follows/" + created.get("id").asInt()).with(csrf()), session).andExpect(status().isNoContent());
        assertThat(body(call(get("/api/me/follows"), session))).hasSize(2);
    }

    @Test
    void cannotTouchOthersFollows() throws Exception {
        int audienceFollowId = body(call(get("/api/me/follows"), loginAudience())).get(0).get("id").asInt();
        MockHttpSession band = login("band@demo.tw", DemoData.PASSWORD);
        call(delete("/api/me/follows/" + audienceFollowId).with(csrf()), band).andExpect(status().isNotFound());
    }

    // ---------- 我的通知 ----------

    @Test
    void notificationsAreMineOnly() throws Exception {
        User audience = users.findByEmail("audience@demo.tw").orElseThrow();
        Event event = events.findAll().get(0);
        Notification n = notifications.save(new Notification(audience, event, "霧島樂隊 有新演出"));

        MockHttpSession session = loginAudience();
        JsonNode list = body(call(get("/api/me/notifications"), session).andExpect(status().isOk()));
        assertThat(list).hasSize(1);
        assertThat(list.get(0).get("is_read").asBoolean()).isFalse();

        // 別人不能把我的通知標成已讀
        call(post("/api/me/notifications/" + n.getId() + "/read").with(csrf()), login("band@demo.tw", DemoData.PASSWORD))
                .andExpect(status().isNotFound());

        JsonNode read = body(call(post("/api/me/notifications/" + n.getId() + "/read").with(csrf()), session)
                .andExpect(status().isOk()));
        assertThat(read.get("is_read").asBoolean()).isTrue();
        assertThat(body(call(get("/api/me/notifications?unread_only=true"), session))).isEmpty();
    }
}
