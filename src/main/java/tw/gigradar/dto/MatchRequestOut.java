package tw.gigradar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tw.gigradar.model.MatchRequest;

public record MatchRequestOut(Integer id, Integer slotId, String status, String message, BigDecimal finalRent,
                              LocalDateTime createdAt, PerformerOut performer) {

    public static MatchRequestOut from(MatchRequest r) {
        return new MatchRequestOut(r.getId(), r.getSlot().getId(), r.getStatus().name(), r.getMessage(),
                r.getFinalRent(), r.getCreatedAt(), PerformerOut.from(r.getPerformer()));
    }
}
