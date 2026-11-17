"""爬蟲共用介面。

每個來源寫一個獨立模組（crawler/sources/<name>.py），實作 Source.fetch()，
回傳統一格式的 RawEvent 清單 —— 這就是 DFD 1.1「擷取原始資料」的輸出。
後面的 1.2 整理格式、1.3 比對寫入都在 crawler/pipeline.py，所有來源共用。
"""
from abc import ABC, abstractmethod
from dataclasses import dataclass, field


@dataclass
class RawEvent:
    """只擷取事實欄位並附來源連結（提案書限制與假設）。欄位都先存原始字串，格式整理交給 pipeline。"""

    title: str
    venue_name: str
    date_text: str                 # 例："2026/12/05"、"12月5日(六)"
    time_text: str | None = None   # 例："19:30"、"晚上7:30"
    performers: list[str] = field(default_factory=list)
    genre: str | None = None
    ticket_url: str | None = None
    source_url: str | None = None


class Source(ABC):
    #: 對應 crawl_sources.module
    module_name: str

    @abstractmethod
    def fetch(self) -> list[RawEvent]:
        """1.1 擷取原始資料：抓網頁 / API，解析成 RawEvent。"""
