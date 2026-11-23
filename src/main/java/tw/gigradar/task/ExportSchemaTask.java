package tw.gigradar.task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 由 model 產生 db/schema.sql（MySQL 語法），讓 Java class 與 SQL 永遠一致。
 * 用法：.\mvnw spring-boot:run "-Dspring-boot.run.arguments=export-schema"
 *
 * <p>DDL 本身由 Hibernate 在啟動時寫出（見 application-export-schema.properties），
 * 這裡只在檔案開頭補上建資料庫的語法。不需要連到 MySQL。
 */
@Component
@Profile("export-schema")
public class ExportSchemaTask implements CommandLineRunner {

    static final Path OUT = Path.of("db", "schema.sql");

    private static final String HEADER = """
            -- Gig Radar 演出雷達 資料庫結構（由 export-schema 工作自動產生，請勿手改）
            CREATE DATABASE IF NOT EXISTS gig_radar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            USE gig_radar;
            """;

    @Override
    public void run(String... args) throws IOException {
        String ddl = Files.readString(OUT, StandardCharsets.UTF_8);
        Files.writeString(OUT, HEADER + ddl, StandardCharsets.UTF_8);
        long tables = ddl.lines().filter(l -> l.strip().startsWith("create table")).count();
        System.out.println("wrote " + OUT + " (" + tables + " tables)");
    }
}
