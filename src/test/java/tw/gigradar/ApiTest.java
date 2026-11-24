package tw.gigradar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tw.gigradar.task.DemoData;

/** API 測試：每個測試前放入示範資料，測試結束自動復原（@Transactional）。不需要 MySQL。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired DemoData demoData;

    @BeforeEach
    void seed() {
        demoData.seed();
    }

    private JsonNode getJson(String url) throws Exception {
        String body = mvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private static Iterable<JsonNode> each(JsonNode array) {
        return array::elements;
    }

    @Test
    void health() throws Exception {
        assertThat(getJson("/api/health").get("status").asText()).isEqualTo("ok");
    }

    @Test
    void searchByDistrict() throws Exception {
        JsonNode events = getJson("/api/events?district=中正區");
        assertThat(events).isNotEmpty();
        for (JsonNode e : each(events)) {
            assertThat(e.at("/venue/district").asText()).isEqualTo("中正區");
        }
    }

    @Test
    void searchByPerformerAlias() throws Exception {
        // 用英文別名也找得到霧島樂隊（含共演場）
        JsonNode events = getJson("/api/events?performer=Kirishima");
        assertThat(events).hasSize(2);
        for (JsonNode e : each(events)) {
            assertThat(StreamSupport.stream(each(e.get("performers")).spliterator(), false)
                    .anyMatch(p -> p.get("name").asText().equals("霧島樂隊"))).isTrue();
        }
    }

    @Test
    void eventJsonUsesSnakeCase() throws Exception {
        JsonNode e = getJson("/api/events").get(0);
        assertThat(e.has("event_date")).isTrue();
        assertThat(e.has("ticket_url")).isTrue();
        assertThat(e.at("/start_time").asText()).matches("\\d{2}:\\d{2}(:\\d{2})?");
    }

    @Test
    void missingEventReturns404WithDetail() throws Exception {
        String body = mvc.perform(get("/api/events/999999")).andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(body).get("detail").asText()).isEqualTo("找不到這場演出");
    }

    @Test
    void openSlotsUseLastMinuteRent() throws Exception {
        JsonNode slots = getJson("/api/slots");
        assertThat(slots).hasSize(4);
        JsonNode deal = StreamSupport.stream(each(slots).spliterator(), false)
                .filter(s -> !s.get("last_minute_rent").isNull()).findFirst().orElseThrow();
        assertThat(deal.get("effective_rent").decimalValue()).isEqualByComparingTo(deal.get("last_minute_rent").decimalValue());
    }

    @Test
    void coPerformanceFilter() throws Exception {
        JsonNode slots = getJson("/api/slots?co_performance_only=true");
        assertThat(slots).isNotEmpty();
        for (JsonNode s : each(slots)) {
            assertThat(s.get("allow_co_performance").asBoolean()).isTrue();
        }
    }

    @Test
    void currentCommissionRulesForAdmin() throws Exception {
        String body = mvc.perform(get("/api/admin/commission-rules/current").with(user("admin@demo.tw").roles("ADMIN")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Set<String> types = new HashSet<>();
        for (JsonNode r : each(json.readTree(body))) {
            types.add(r.get("rule_type").asText());
        }
        assertThat(types).containsExactlyInAnyOrder("match", "merch");
    }

    @Test
    void adminApiIsClosedToOthers() throws Exception {
        mvc.perform(get("/api/admin/commission-rules/current")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/commission-rules/current").with(user("audience@demo.tw").roles("AUDIENCE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void frontendIsServed() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk());
        mvc.perform(get("/slots.html")).andExpect(status().isOk());
        mvc.perform(get("/login.html")).andExpect(status().isOk());
        mvc.perform(get("/account.html")).andExpect(status().isOk());
    }
}
