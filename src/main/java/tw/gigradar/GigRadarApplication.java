package tw.gigradar;

import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Gig Radar 演出雷達 —— 程式進入點。
 *
 * <pre>
 * 啟動網站：      .\mvnw spring-boot:run
 * 建表＋示範資料：.\mvnw spring-boot:run "-Dspring-boot.run.arguments=seed"
 * 跑爬蟲：        .\mvnw spring-boot:run "-Dspring-boot.run.arguments=crawl"
 * 匯出 schema：   .\mvnw spring-boot:run "-Dspring-boot.run.arguments=export-schema"
 * </pre>
 *
 * 第一個參數是工作名稱時（seed / crawl / export-schema），不開網站，
 * 改啟用同名的 Spring profile（見 tw.gigradar.task 與 application-*.properties），做完就結束。
 */
@SpringBootApplication
public class GigRadarApplication {

    static final Set<String> TASKS = Set.of("seed", "crawl", "export-schema");

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GigRadarApplication.class);
        if (args.length == 0 || !TASKS.contains(args[0])) {
            app.run(args);
            return;
        }
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles(args[0]);
        System.exit(SpringApplication.exit(app.run(args)));
    }
}
