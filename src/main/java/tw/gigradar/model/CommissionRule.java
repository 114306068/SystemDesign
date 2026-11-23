package tw.gigradar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import tw.gigradar.model.enums.CommissionRuleType;

/**
 * D7 平台管理者設定的抽成比例。調整時新增一筆而不是覆蓋，
 * 以 effectiveFrom 最新且已生效的一筆為準（保留歷史，舊帳不受影響）。
 */
@Entity
@Table(name = "commission_rules")
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionRuleType ruleType;

    /** 0.1000 = 10% */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CommissionRule() {}

    public CommissionRule(CommissionRuleType ruleType, BigDecimal rate, LocalDate effectiveFrom, User createdBy) {
        this.ruleType = ruleType;
        this.rate = rate;
        this.effectiveFrom = effectiveFrom;
        this.createdBy = createdBy;
    }

    public Integer getId() { return id; }
    public CommissionRuleType getRuleType() { return ruleType; }
    public BigDecimal getRate() { return rate; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public User getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
