# Gig Radar 演出雷達｜演出資訊整合及場館媒合平台

系統分析與設計 第 7 組（張皓鈞、李宥叡、黃鼎淯、賈茹茵）

技術：Java 21 · Spring Boot 3.5 · Spring Data JPA（Hibernate）· MySQL 8 · 純 HTML/CSS/JS 前端

## 快速開始（Windows PowerShell）

需要先裝好 **JDK 21**。Maven 不用另外裝，專案附了 Maven Wrapper（`mvnw.cmd`），第一次執行會自動下載。

```powershell
# 1. 準備 MySQL（擇一）
docker compose up -d               # A. 用 Docker（要先開 Docker Desktop）
# B. 自己的 MySQL：執行下面的 SQL 建資料庫與帳號
#    CREATE DATABASE gig_radar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
#    CREATE USER 'gigradar'@'localhost' IDENTIFIED BY 'gigradar';
#    GRANT ALL ON gig_radar.* TO 'gigradar'@'localhost';

copy .env.example .env             # 帳密不同的話改 .env；沒有 MySQL 也可以照 .env 裡的說明改用 H2

# 2. 建表 + 放示範資料（會清空資料庫）
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=seed"

# 3. 跑一次範例爬蟲（會合併重複演出、對樂團別名、通知追蹤者）
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=crawl"

# 4. 啟動網站（停止：在同一個視窗按 Ctrl+C）
.\mvnw spring-boot:run
```

macOS / Linux 把 `.\mvnw` 換成 `./mvnw`、`copy` 換成 `cp`。

- 前端：<http://127.0.0.1:8000/>（演出查詢）、<http://127.0.0.1:8000/slots.html>（場地空檔）
- API 文件（可直接在網頁上試打）：<http://127.0.0.1:8000/docs>
- 示範帳號密碼皆為 `demo1234`：`audience@demo.tw`、`band@demo.tw`、`venue@demo.tw`、`admin@demo.tw`
- 登入：<http://127.0.0.1:8000/login.html>，登入後右上角進「我的帳號」看通知、管理追蹤、改個人資料
- 在 /docs 試需要登入的 API：先在同一個瀏覽器登入網站，再回 /docs 按 Try it out

測試不需要 MySQL（用 H2 記憶體資料庫）：`.\mvnw test`

用 IntelliJ IDEA / VS Code 開啟：直接開專案資料夾，IDE 會讀 `pom.xml`；執行 `GigRadarApplication` 的 `main` 就是啟動網站。

## 專案結構（對應期中報告 DFD）

```
src/main/java/tw/gigradar/
  GigRadarApplication.java   程式進入點；第一個參數是 seed / crawl / export-schema 時改跑該工作
  model/                     資料表（JPA Entity），依 D1–D7 分檔（見 docs/ERD.md）
    enums/                   狀態欄位的列舉值
  repository/                資料存取（Spring Data，大多只要宣告方法名稱）
  dto/                       API 回傳格式（決定哪些欄位給外面看）
  web/
    EventController          2.1 查詢演出                    ✅
    AuthController           註冊 / 登入（登出由 SecurityConfig 處理）✅
    MeController             我的帳號：個人資料、改密碼、2.2 追蹤、2.3 通知 ✅
    MatchingController       3.0 檔期與媒合                  ◐ 查空檔已完成
    MerchController          4.0 周邊預購                    ◐ 商品列表已完成
    BillingController        5.0 抽成與推廣                  ◐ 抽成規則查詢已完成
    CatalogController        場館 / 表演者 / 地區 / 類型下拉選單
    EventSpecs               動態查詢條件（有填才加進 WHERE）
  security/                  Spring Security 設定：Session 登入、CSRF、哪些 API 要登入 / 限管理者
  service/
    AccountService           註冊、個人資料、改密碼
    FollowService            2.2 追蹤 / 取消、2.3 我的通知、標為已讀
    DedupService             1.3 去重（日期＋場館＋開演時間）與別名比對
    NotifyService            2.3 新演出通知
    BillingService           5.0 取得目前生效的抽成規則
    NameNormalizer           名稱正規化（全半形、大小寫、空白）
    PasswordHasher           密碼雜湊（PBKDF2）
  crawler/
    RawEvent / Source        統一格式 + 來源介面
    CrawlerPipeline          1.1 擷取 → 1.2 整理格式 → 1.3 比對寫入，含抓取筆數異常偵測
    sources/                 每個來源一個 class；SampleJsonSource 是不連網的範例
  task/                      seed 示範資料、crawl 爬蟲、export-schema 產生 db/schema.sql
src/main/resources/
  application.properties     設定（資料庫帳密從 .env 讀）
  application-*.properties   各工作（seed / crawl / export-schema）專用設定
  static/                    前端：index、slots、login（登入註冊）、account（我的帳號）、app.js、style.css
  crawler/sample_events.json 範例爬蟲資料
src/test/java/               JUnit 測試
db/schema.sql                MySQL DDL（自動產生，交 ERD 作業可附）
docs/ERD.md                  ERD（Mermaid，GitHub 上直接顯示圖）與設計決定
```

每個 Controller 檔案最上面的註解列了該模組**待實作的 API**，可以直接照著分工。

## 改資料表的流程

1. 改 `model/` 裡的 class
2. `.\mvnw spring-boot:run "-Dspring-boot.run.arguments=export-schema"` 更新 `db/schema.sql`
3. 同步更新 `docs/ERD.md`
4. 開發階段直接跑 `seed` 重建（還沒有正式資料，不需要 migration）

## 新增爬蟲來源（期中後第 2 週 PoC）

1. 複製 `crawler/sources/SampleJsonSource.java` → `crawler/sources/<來源名>Source.java`，改 `moduleName()`
2. `fetch()` 裡改成抓網頁（建議在 `pom.xml` 加 `jsoup`），輸出 `RawEvent` 清單；只擷取事實欄位並附 `sourceUrl`
3. 在 `crawl_sources` 新增一筆（`module` 填 `moduleName()`、`expected_min_count` 填正常情況下至少會抓到幾筆）
4. `.\mvnw spring-boot:run "-Dspring-boot.run.arguments=crawl <來源名>"`
   爬到不認得的場館會被跳過並列出來 → 在 `venue_aliases` 加對應名稱即可

## 下一步（雛型開發 I）

核心流程：**空檔上架 → 媒合 → 演出上架 → 通知追蹤者 → 周邊取貨**

- [x] 登入 / 註冊、個人資料
- [ ] 3.1 / 3.3 / 3.4 / 3.5 媒合流程（成交時建立 Event 並呼叫 `NotifyService.notifyFollowers`）
- [x] 2.2 新增 / 取消追蹤、2.3 我的通知
- [ ] 4.1–4.3 周邊上架、預購、取貨憑證與取貨清單
- [ ] 5.1 成交時寫入 transactions 抽成；5.3 模擬付款
- [ ] 第一個真實爬蟲來源 PoC
