package tw.gigradar.model.enums;

/** 爬蟲執行結果 */
public enum CrawlRunStatus {
    success,
    anomaly,    // 抓到的筆數異常偏低 → 通知維護者
    failed;
}
