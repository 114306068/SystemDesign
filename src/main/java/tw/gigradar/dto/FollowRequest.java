package tw.gigradar.dto;

import jakarta.validation.constraints.NotNull;
import tw.gigradar.model.enums.FollowTarget;

/** 新增追蹤：target_type 是 performer 時填 performer_id，venue 填 venue_id，genre 填 genre。 */
public record FollowRequest(
        @NotNull(message = "請選擇追蹤類型") FollowTarget targetType,
        Integer performerId,
        Integer venueId,
        String genre) {}
