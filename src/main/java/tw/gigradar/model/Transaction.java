package tw.gigradar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import tw.gigradar.model.enums.PaymentStatus;
import tw.gigradar.model.enums.TxnType;

/**
 * D7 交易紀錄（平台收入）。三種來源：媒合抽成、周邊抽成、付費曝光，
 * 依 txnType 只會填 matchRequest / order / promotion 其中一個。
 * commissionRate 存成交當下的抽成比例，之後改規則不影響舊帳。
 */
@Entity
@Table(name = "transactions", indexes = @Index(name = "ix_transactions_txn_type", columnList = "txn_type"))
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TxnType txnType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id")
    private MatchRequest matchRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    /** 場租 / 訂單金額 / 曝光費 */
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal grossAmount;

    @Column(precision = 5, scale = 4)
    private BigDecimal commissionRate;

    /** 平台收入 */
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal commissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.pending;

    /** 模擬金流回傳編號 */
    @Column(length = 100)
    private String paymentRef;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    protected Transaction() {}

    public Transaction(TxnType txnType, BigDecimal grossAmount, BigDecimal commissionRate, BigDecimal commissionAmount) {
        this.txnType = txnType;
        this.grossAmount = grossAmount;
        this.commissionRate = commissionRate;
        this.commissionAmount = commissionAmount;
    }

    public Integer getId() { return id; }
    public TxnType getTxnType() { return txnType; }
    public MatchRequest getMatchRequest() { return matchRequest; }
    public void setMatchRequest(MatchRequest matchRequest) { this.matchRequest = matchRequest; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
