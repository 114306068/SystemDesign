"""範例來源：從本機 JSON 檔讀取，用來測試整條 pipeline，不連網。

寫真正的來源（售票網站 / 場館官網）時，複製這個檔案，把 fetch() 改成
requests + BeautifulSoup（或該網站的 API），輸出一樣的 RawEvent 即可。
"""
import json
from pathlib import Path

from crawler.base import RawEvent, Source

DATA_FILE = Path(__file__).with_name("sample_events.json")


class SampleJsonSource(Source):
    module_name = "sample_json"

    def __init__(self, path: Path = DATA_FILE):
        self.path = path

    def fetch(self) -> list[RawEvent]:
        rows = json.loads(self.path.read_text(encoding="utf-8"))
        return [RawEvent(**row) for row in rows]


SOURCE = SampleJsonSource
