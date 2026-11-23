package tw.gigradar.crawler;

import java.util.List;

/**
 * 爬蟲來源介面。每個網站寫一個 class 實作它，並加上 {@code @Component}，
 * Spring 會自動收集，{@link CrawlerPipeline} 依 crawl_sources.module 找到對應的來源。
 */
public interface Source {

    /** 對應資料表 crawl_sources.module */
    String moduleName();

    /** 1.1 擷取原始資料：抓網頁 / API，解析成 RawEvent。 */
    List<RawEvent> fetch();
}
