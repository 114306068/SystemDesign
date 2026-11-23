package tw.gigradar.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.gigradar.dto.ProductOut;
import tw.gigradar.repository.ProductRepository;

/**
 * 4.0 處理周邊預購。
 *
 * <p>已完成：列出表演者的商品。
 * <p>待實作：
 * <ul>
 *   <li>4.1 POST /api/performers/{id}/products   表演者上架商品</li>
 *   <li>4.2 POST /api/orders                     觀眾預購（檢查庫存、鎖定單價、產生 pickup_code 取貨憑證）</li>
 *   <li>4.3 GET  /api/events/{id}/pickup-list    演出當日取貨清單（給表演者）</li>
 *   <li>POST /api/orders/{id}/pickup             現場核銷取貨</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
@Tag(name = "4.0 周邊預購")
@Transactional(readOnly = true)
public class MerchController {

    private final ProductRepository products;

    public MerchController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping("/performers/{performerId}/products")
    public List<ProductOut> products(@PathVariable int performerId) {
        return products.findByPerformerIdAndActiveTrueOrderByIdAsc(performerId).stream().map(ProductOut::from).toList();
    }
}
