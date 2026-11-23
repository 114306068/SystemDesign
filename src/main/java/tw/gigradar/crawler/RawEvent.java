package tw.gigradar.crawler;

import java.util.List;

/**
 * 爬蟲抓下來的原始資料（DFD 1.1「擷取原始資料」的輸出）。所有來源都輸出這個統一格式。
 * 只擷取事實欄位並附來源連結（提案書限制與假設）。日期時間先存原始字串，格式整理交給 {@link CrawlerPipeline}。
 *
 * @param dateText 例："2026/12/05"、"12月5日(六)"
 * @param timeText 例："19:30"、"晚上7:30"
 */
public record RawEvent(
        String title,
        String venueName,
        String dateText,
        String timeText,
        List<String> performers,
        String genre,
        String ticketUrl,
        String sourceUrl) {

    public RawEvent {
        performers = performers == null ? List.of() : List.copyOf(performers);
    }
}
