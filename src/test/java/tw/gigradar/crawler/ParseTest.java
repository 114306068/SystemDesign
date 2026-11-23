package tw.gigradar.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tw.gigradar.crawler.CrawlerPipeline.parseDate;
import static tw.gigradar.crawler.CrawlerPipeline.parseTime;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import tw.gigradar.service.NameNormalizer;
import tw.gigradar.service.PasswordHasher;

/** 不需要資料庫的單元測試：1.2 整理格式、名稱正規化、密碼雜湊。 */
class ParseTest {

    @Test
    void parseDateFormats() {
        LocalDate today = LocalDate.of(2026, 11, 17);
        assertThat(parseDate("2026/12/05", today)).isEqualTo(LocalDate.of(2026, 12, 5));
        assertThat(parseDate("2026-12-05", today)).isEqualTo(LocalDate.of(2026, 12, 5));
        assertThat(parseDate("12月5日(六)", today)).isEqualTo(LocalDate.of(2026, 12, 5));
        assertThat(parseDate("1/10", today)).isEqualTo(LocalDate.of(2027, 1, 10)); // 沒寫年份 → 往後推
        assertThatThrownBy(() -> parseDate("近期公布", today)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseTimeFormats() {
        assertThat(parseTime("19:30")).isEqualTo(LocalTime.of(19, 30));
        assertThat(parseTime("晚上7:30")).isEqualTo(LocalTime.of(19, 30));
        assertThat(parseTime("下午3點")).isEqualTo(LocalTime.of(15, 0));
        assertThat(parseTime(null)).isNull();
    }

    @Test
    void normalizeIgnoresWidthCaseSpaces() {
        assertThat(NameNormalizer.normalize("北岸 LIVE HOUSE")).isEqualTo(NameNormalizer.normalize("北岸Live House"));
        assertThat(NameNormalizer.normalize("ＡＢＣ　Band")).isEqualTo("abcband");
    }

    @Test
    void passwordHashRoundTrip() {
        String stored = PasswordHasher.hash("demo1234");
        assertThat(stored).startsWith("pbkdf2_sha256$200000$");
        assertThat(PasswordHasher.verify("demo1234", stored)).isTrue();
        assertThat(PasswordHasher.verify("wrong", stored)).isFalse();
    }
}
