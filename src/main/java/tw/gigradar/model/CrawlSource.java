package tw.gigradar.model;

import jakarta.persistence.*;

/** D2 爬蟲來源（由平台管理者設定）。module 對應 crawler 套件裡某個 Source 的 moduleName()。 */
@Entity
@Table(name = "crawl_sources")
public class CrawlSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String module;

    @Column(length = 500)
    private String baseUrl;

    /** 低於此筆數視為異常（網站可能改版） */
    @Column(nullable = false)
    private int expectedMinCount = 1;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected CrawlSource() {}

    public CrawlSource(String name, String module, int expectedMinCount) {
        this.name = name;
        this.module = module;
        this.expectedMinCount = expectedMinCount;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public int getExpectedMinCount() { return expectedMinCount; }
    public void setExpectedMinCount(int expectedMinCount) { this.expectedMinCount = expectedMinCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
