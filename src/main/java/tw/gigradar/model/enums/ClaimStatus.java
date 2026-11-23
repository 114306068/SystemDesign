package tw.gigradar.model.enums;

/** 場館認領狀態 */
public enum ClaimStatus {
    unclaimed,    // 爬蟲建立、尚未認領
    pending,    // 已申請認領，待管理者審核
    approved,
    rejected;
}
