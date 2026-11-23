package tw.gigradar.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import tw.gigradar.model.VenueSlot;

public record SlotOut(Integer id, LocalDate slotDate, LocalTime startTime, LocalTime endTime, BigDecimal rent,
                      BigDecimal lastMinuteRent, BigDecimal effectiveRent, LocalDateTime applyDeadline,
                      boolean allowCoPerformance, int maxPerformers, String status, VenueOut venue) {

    public static SlotOut from(VenueSlot s) {
        return new SlotOut(s.getId(), s.getSlotDate(), s.getStartTime(), s.getEndTime(), s.getRent(),
                s.getLastMinuteRent(), s.getEffectiveRent(), s.getApplyDeadline(), s.isAllowCoPerformance(),
                s.getMaxPerformers(), s.getStatus().name(), VenueOut.from(s.getVenue()));
    }
}
