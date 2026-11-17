"""Gig Radar 演出雷達 —— FastAPI 進入點。

啟動：uvicorn app.main:app --reload
API 文件：http://127.0.0.1:8000/docs
前端：    http://127.0.0.1:8000/
"""
from pathlib import Path

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from app.routers import billing, catalog, events, follows, matching, merch

app = FastAPI(title="Gig Radar 演出雷達", version="0.1.0")

for r in (events, catalog, matching, follows, merch, billing):
    app.include_router(r.router)


@app.get("/api/health", tags=["系統"])
def health():
    return {"status": "ok"}


FRONTEND = Path(__file__).resolve().parent.parent / "frontend"
app.mount("/", StaticFiles(directory=FRONTEND, html=True), name="frontend")
