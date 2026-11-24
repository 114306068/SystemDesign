package tw.gigradar.dto;

import java.time.LocalDateTime;
import tw.gigradar.model.Follow;

/** 我的追蹤：依 target_type，performer / venue / genre 只會有一個有值。 */
public record FollowOut(Integer id, String targetType, PerformerOut performer, VenueOut venue, String genre,
                        LocalDateTime createdAt) {

    public static FollowOut from(Follow f) {
        return new FollowOut(f.getId(), f.getTargetType().name(),
                f.getPerformer() != null ? PerformerOut.from(f.getPerformer()) : null,
                f.getVenue() != null ? VenueOut.from(f.getVenue()) : null,
                f.getGenre(), f.getCreatedAt());
    }
}
