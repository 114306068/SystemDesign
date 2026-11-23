package tw.gigradar.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.CommissionRule;
import tw.gigradar.model.enums.CommissionRuleType;

public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Integer> {

    /** 某類型在指定日期當下生效的規則：effectiveFrom ≤ 該日 中最新的一筆。 */
    Optional<CommissionRule> findFirstByRuleTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescIdDesc(
            CommissionRuleType ruleType, LocalDate on);
}
