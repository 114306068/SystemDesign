package tw.gigradar.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.gigradar.dto.CommissionRuleOut;
import tw.gigradar.model.enums.CommissionRuleType;
import tw.gigradar.service.BillingService;

/**
 * 5.0 結算抽成與推廣。
 *
 * <p>已完成：查詢目前生效的抽成規則。
 * <p>待實作：
 * <ul>
 *   <li>5.1 BillingService.calcCommission()：媒合成交 / 訂單付款時算抽成並寫入 transactions</li>
 *   <li>5.2 POST /api/promotions                 表演者購買付費曝光（NT$500）</li>
 *   <li>5.3 POST /api/payments/{txnId}/mock-pay  模擬金流回傳付款結果</li>
 *   <li>5.4 GET  /api/admin/reports/monthly      營運報表</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "5.0 帳務與推廣")
public class BillingController {

    private final BillingService billing;

    public BillingController(BillingService billing) {
        this.billing = billing;
    }

    @GetMapping("/commission-rules/current")
    public List<CommissionRuleOut> currentRules() {
        LocalDate today = LocalDate.now();
        return Arrays.stream(CommissionRuleType.values())
                .map(type -> billing.currentRule(type, today))
                .flatMap(Optional::stream)
                .map(CommissionRuleOut::from)
                .toList();
    }
}
