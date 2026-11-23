package tw.gigradar.task;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tw.gigradar.crawler.CrawlerPipeline;
import tw.gigradar.model.CrawlRun;
import tw.gigradar.model.CrawlSource;
import tw.gigradar.repository.CrawlSourceRepository;

/**
 * 跑爬蟲。
 * <pre>
 * .\mvnw spring-boot:run "-Dspring-boot.run.arguments=crawl"              跑所有啟用中的來源
 * .\mvnw spring-boot:run "-Dspring-boot.run.arguments=crawl sample_json"  只跑一個來源
 * </pre>
 */
@Component
@Profile("crawl")
public class CrawlTask implements CommandLineRunner {

    private final CrawlSourceRepository sources;
    private final CrawlerPipeline pipeline;

    public CrawlTask(CrawlSourceRepository sources, CrawlerPipeline pipeline) {
        this.sources = sources;
        this.pipeline = pipeline;
    }

    @Override
    public void run(String... args) {
        // args[0] 是 "crawl"，後面是要跑的來源 module 名稱
        List<String> modules = Arrays.stream(args).skip(1).filter(a -> !a.startsWith("--")).toList();
        List<CrawlSource> targets = modules.isEmpty() ? sources.findByActiveTrue() : sources.findByActiveTrueAndModuleIn(modules);
        if (targets.isEmpty()) {
            System.out.println("沒有可執行的爬蟲來源（先跑 seed，或檢查 crawl_sources 的 module / is_active）");
        }
        for (CrawlSource source : targets) {
            CrawlRun run = pipeline.runSource(source);
            System.out.printf("%s: 抓到 %d、新增 %d、合併 %d、狀態 %s%n", source.getName(),
                    run.getFetchedCount(), run.getInsertedCount(), run.getMergedCount(), run.getStatus());
            if (run.getMessage() != null) {
                System.out.println("  " + run.getMessage().replace("\n", "\n  "));
            }
        }
    }
}
