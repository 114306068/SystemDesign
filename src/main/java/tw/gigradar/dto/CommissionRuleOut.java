package tw.gigradar.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import tw.gigradar.model.CommissionRule;

public record CommissionRuleOut(Integer id, String ruleType, BigDecimal rate, LocalDate effectiveFrom) {

    public static CommissionRuleOut from(CommissionRule r) {
        return new CommissionRuleOut(r.getId(), r.getRuleType().name(), r.getRate(), r.getEffectiveFrom());
    }
}
