"""建立所有資料表（資料庫本身要先存在，見 README）。

用法：python -m scripts.init_db          # 建表（已存在的表不動）
      python -m scripts.init_db --reset  # 刪掉全部重建（會清空資料！）
"""
import sys

import app.models  # noqa: F401
from app.database import Base, engine


def main() -> None:
    if "--reset" in sys.argv:
        Base.metadata.drop_all(engine)
        print("已刪除所有資料表")
    Base.metadata.create_all(engine)
    print(f"資料表就緒：{len(Base.metadata.tables)} 張")


if __name__ == "__main__":
    main()
