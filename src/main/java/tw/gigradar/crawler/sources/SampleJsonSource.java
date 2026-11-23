package tw.gigradar.crawler.sources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tw.gigradar.crawler.RawEvent;
import tw.gigradar.crawler.Source;

/**
 * 範例來源：讀取 src/main/resources/crawler/sample_events.json，用來測試整條 pipeline，不連網。
 *
 * <p>寫真正的來源（售票網站 / 場館官網）時，複製這個檔案，把 fetch() 改成抓網頁
 * （建議 jsoup 套件）或呼叫該網站的 API，輸出一樣的 RawEvent 即可。
 */
@Component
public class SampleJsonSource implements Source {

    private static final String DATA_FILE = "crawler/sample_events.json";

    private final ObjectMapper json;

    public SampleJsonSource(ObjectMapper json) {
        this.json = json; // Spring 設定好的 ObjectMapper，JSON 的 snake_case 欄位會對到 RawEvent
    }

    @Override
    public String moduleName() {
        return "sample_json";
    }

    @Override
    public List<RawEvent> fetch() {
        try (InputStream in = new ClassPathResource(DATA_FILE).getInputStream()) {
            return json.readValue(in, new TypeReference<>() {});
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
