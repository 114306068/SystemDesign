def test_health(client):
    assert client.get("/api/health").json() == {"status": "ok"}


def test_search_by_district(client):
    events = client.get("/api/events", params={"district": "中正區"}).json()
    assert events and all(e["venue"]["district"] == "中正區" for e in events)


def test_search_by_performer_alias(client):
    # 用英文別名也找得到霧島樂隊（含共演場）
    events = client.get("/api/events", params={"performer": "Kirishima"}).json()
    assert len(events) == 2
    assert all(any(p["name"] == "霧島樂隊" for p in e["performers"]) for e in events)


def test_open_slots_use_last_minute_rent(client):
    slots = client.get("/api/slots").json()
    assert len(slots) == 4
    deal = next(s for s in slots if s["last_minute_rent"])
    assert deal["effective_rent"] == deal["last_minute_rent"]


def test_co_performance_filter(client):
    slots = client.get("/api/slots", params={"co_performance_only": True}).json()
    assert slots and all(s["allow_co_performance"] for s in slots)


def test_current_commission_rules(client):
    rules = client.get("/api/admin/commission-rules/current").json()
    assert {r["rule_type"] for r in rules} == {"match", "merch"}
