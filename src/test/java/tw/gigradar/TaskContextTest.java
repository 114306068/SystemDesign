package tw.gigradar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import tw.gigradar.crawler.CrawlerPipeline;
import tw.gigradar.task.DemoData;

/**
 * seed / crawl / export-schema 不開網站（WebApplicationType.NONE）。
 * 確認在沒有網頁層時程式也能啟動 —— 登入相關的設定只能在網站模式載入。
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class TaskContextTest {

    @Autowired ApplicationContext context;

    @Test
    void startsWithoutWebLayer() {
        assertThat(context.getBean(DemoData.class)).isNotNull();
        assertThat(context.getBean(CrawlerPipeline.class)).isNotNull();
    }
}
