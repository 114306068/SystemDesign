/**
 * 所有資料表（JPA Entity）。依期中報告 DFD 的資料儲存分組：
 *
 * <ul>
 *   <li>D1 會員資料：{@link tw.gigradar.model.User}、{@link tw.gigradar.model.Performer}、
 *       {@link tw.gigradar.model.PerformerAlias}、{@link tw.gigradar.model.Venue}、{@link tw.gigradar.model.VenueAlias}</li>
 *   <li>D2 演出資訊：{@link tw.gigradar.model.Event}（含 event_performers 關聯表）、
 *       {@link tw.gigradar.model.CrawlSource}、{@link tw.gigradar.model.CrawlRun}</li>
 *   <li>D3 場地檔期：{@link tw.gigradar.model.VenueSlot}</li>
 *   <li>D4 媒合紀錄：{@link tw.gigradar.model.MatchRequest}</li>
 *   <li>D5 追蹤清單：{@link tw.gigradar.model.Follow}、{@link tw.gigradar.model.Notification}</li>
 *   <li>D6 商品與訂單：{@link tw.gigradar.model.Product}、{@link tw.gigradar.model.Order}、{@link tw.gigradar.model.OrderItem}</li>
 *   <li>D7 交易紀錄：{@link tw.gigradar.model.Transaction}、{@link tw.gigradar.model.CommissionRule}、
 *       {@link tw.gigradar.model.Promotion}</li>
 * </ul>
 *
 * <p>欄位名稱由 Spring Boot 自動把 camelCase 轉成 snake_case（eventDate → event_date）。
 */
package tw.gigradar.model;
