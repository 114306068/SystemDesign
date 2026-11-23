package tw.gigradar.model.enums;

/** 場地空檔狀態 */
public enum SlotStatus {
    open,    // 空檔，可申請
    booked,    // 已媒合成功
    closed;    // 場地方下架 / 過期
}
