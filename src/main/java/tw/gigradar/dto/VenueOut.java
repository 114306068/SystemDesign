package tw.gigradar.dto;

import tw.gigradar.model.Venue;

public record VenueOut(Integer id, String name, String district, String address, Integer capacity,
                       String officialUrl, String claimStatus) {

    public static VenueOut from(Venue v) {
        return new VenueOut(v.getId(), v.getName(), v.getDistrict(), v.getAddress(), v.getCapacity(),
                v.getOfficialUrl(), v.getClaimStatus().name());
    }
}
