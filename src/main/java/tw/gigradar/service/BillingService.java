package tw.gigradar.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.gigradar.model.CommissionRule;
import tw.gigradar.model.enums.CommissionRuleType;
import tw.gigradar.repository.CommissionRuleRepository;

/**
 * 5.0 結算抽成與推廣。
 *
 * <p>待實作：5.1 calcCommission()：媒合成交 / 訂單付款時，用 {@link #currentRule} 算抽成並寫入 transactions。
 */
@Service
@Transactional(readOnly = true)
public class BillingService {

    private final CommissionRuleRepository rules;

    public BillingService(CommissionRuleRepository rules) {
        this.rules = rules;
    }

    /** 指定日期當下生效的抽成規則。 */
    public Optional<CommissionRule> currentRule(CommissionRuleType type, LocalDate on) {
        return rules.findFirstByRuleTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescIdDesc(type, on);
    }
}
