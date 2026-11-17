# Gig Radar 演出雷達｜演出資訊整合及場館媒合平台

系統分析與設計 第 7 組（張皓鈞、李宥叡、黃鼎淯、賈茹茵）

技術：Python 3.11+ · FastAPI · SQLAlchemy 2 · MySQL 8 · 純 HTML/CSS/JS 前端

## 快速開始

```bash
# 1. 安裝套件（建議先建虛擬環境）
python -m venv .venv
source .venv/bin/activate          # Windows：.venv\Scripts\activate
pip install -r requirements.txt

# 2. 準備 MySQL（擇一）
docker compose up -d               # A. 用 Docker
# B. 自己的 MySQL：執行下面的 SQL 建資料庫與帳號
#    CREATE DATABASE gig_radar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
#    CREATE USER 'gigradar'@'localhost' IDENTIFIED BY 'gigradar';
#    GRANT ALL ON gig_radar.* TO 'gigradar'@'localhost';

cp .env.example .env               # 帳密不同的話改 .env

# 3. 建表 + 放示範資料（會清空資料庫）
python -m scripts.seed

# 4. 跑一次範例爬蟲（會合併重複演出、對樂團別名、通知追蹤者）
python -m crawler.pipeline

# 5. 啟動
uvicorn app.main:app --reload
```

- 前端：<http://127.0.0.1:8000/>（演出查詢）、<http://127.0.0.1:8000/slots.html>（場地空檔）
- API 文件（可直接在網頁上試打）：<http://127.0.0.1:8000/docs>
- 示範帳號密碼皆為 `demo1234`：`audience@demo.tw`、`band@demo.tw`、`venue@demo.tw`、`admin@demo.tw`

測試不需要 MySQL（用 SQLite）：`pytest`

## 專案結構（對應期中報告 DFD）

```
app/
  models/        資料表，依 D1–D7 分檔（見 docs/ERD.md）
  routers/
    events.py    2.1 查詢演出                    ✅
    follows.py   2.2 設定追蹤 / 2.3 通知         ◐ 看通知已完成
    matching.py  3.0 檔期與媒合                  ◐ 查空檔已完成
    merch.py     4.0 周邊預購                    ◐ 商品列表已完成
    billing.py   5.0 抽成與推廣                  ◐ 抽成規則查詢已完成
    catalog.py   場館 / 表演者 / 地區 / 類型下拉選單
  services/
    dedup.py     1.3 去重（日期＋場館＋開演時間）與別名比對
    notify.py    2.3 新演出通知
    security.py  密碼雜湊
crawler/
  base.py        RawEvent 統一格式 + Source 介面
  pipeline.py    1.1 擷取 → 1.2 整理格式 → 1.3 比對寫入，含抓取筆數異常偵測
  sources/       每個來源一個模組；sample_json.py 是不連網的範例
frontend/        index.html、slots.html、app.js、style.css
scripts/         seed.py 示範資料、init_db.py 建表、export_schema.py 產生 db/schema.sql
db/schema.sql    MySQL DDL（自動產生，交 ERD 作業可附）
docs/ERD.md      ERD（Mermaid，GitHub 上直接顯示圖）與設計決定
tests/           pytest
```

每個 router 檔案最上面的註解列了該模組**待實作的 API**，可以直接照著分工。

## 改資料表的流程

1. 改 `app/models/` 裡的 class
2. `python -m scripts.export_schema` 更新 `db/schema.sql`
3. 同步更新 `docs/ERD.md`
4. 開發階段直接 `python -m scripts.seed` 重建（還沒有正式資料，不需要 migration）

## 新增爬蟲來源（期中後第 2 週 PoC）

1. 複製 `crawler/sources/sample_json.py` → `crawler/sources/<來源名>.py`
2. `fetch()` 裡改成抓網頁（建議 `requests` + `beautifulsoup4`），輸出 `RawEvent` 清單；只擷取事實欄位並附 `source_url`
3. 在 `crawl_sources` 新增一筆（`module` 填檔名、`expected_min_count` 填正常情況下至少會抓到幾筆）
4. `python -m crawler.pipeline <來源名>`
   爬到不認得的場館會被跳過並列出來 → 在 `venue_aliases` 加對應名稱即可

## 下一步（雛型開發 I）

核心流程：**空檔上架 → 媒合 → 演出上架 → 通知追蹤者 → 周邊取貨**

- [ ] 登入（目前 API 用網址上的 user_id，之後改成登入後取得）
- [ ] 3.1 / 3.3 / 3.4 / 3.5 媒合流程（成交時建立 Event 並呼叫 `notify_followers`）
- [ ] 2.2 新增 / 取消追蹤
- [ ] 4.1–4.3 周邊上架、預購、取貨憑證與取貨清單
- [ ] 5.1 成交時寫入 transactions 抽成；5.3 模擬付款
- [ ] 第一個真實爬蟲來源 PoC
