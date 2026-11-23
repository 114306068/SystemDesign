package tw.gigradar.dto;

import java.math.BigDecimal;
import tw.gigradar.model.Product;

public record ProductOut(Integer id, Integer performerId, String name, String description, BigDecimal price,
                         int stock, String imageUrl) {

    public static ProductOut from(Product p) {
        return new ProductOut(p.getId(), p.getPerformer().getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getStock(), p.getImageUrl());
    }
}
