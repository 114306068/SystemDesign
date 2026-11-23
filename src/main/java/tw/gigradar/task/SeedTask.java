package tw.gigradar.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 建表＋放示範資料（會清空資料庫，請只在開發環境使用）。
 * 用法：.\mvnw spring-boot:run "-Dspring-boot.run.arguments=seed"
 *
 * <p>清空重建由 application-seed.properties 的 ddl-auto=create 負責：Hibernate 啟動時依 model 重建所有資料表。
 */
@Component
@Profile("seed")
public class SeedTask implements CommandLineRunner {

    private final DemoData demoData;

    public SeedTask(DemoData demoData) {
        this.demoData = demoData;
    }

    @Override
    public void run(String... args) {
        demoData.seed();
        System.out.println("示範資料已建立。帳號密碼皆為 " + DemoData.PASSWORD
                + "：audience@demo.tw / band@demo.tw / venue@demo.tw / admin@demo.tw");
    }
}
