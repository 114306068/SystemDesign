from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """設定值從環境變數或 .env 讀取，見 .env.example。"""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    database_url: str = (
        "mysql+pymysql://gigradar:gigradar@127.0.0.1:3306/gig_radar?charset=utf8mb4"
    )
    sql_echo: bool = False
    # 試辦範圍：台北市
    pilot_city: str = "台北市"


settings = Settings()
