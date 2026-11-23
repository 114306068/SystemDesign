package tw.gigradar.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tw.gigradar.model.Venue;

public interface VenueRepository extends JpaRepository<Venue, Integer> {

    List<Venue> findAllByOrderByDistrictAscNameAsc();

    List<Venue> findByDistrictOrderByNameAsc(String district);

    @Query("select distinct v.district from Venue v order by v.district")
    List<String> findDistinctDistricts();
}
