package tw.gigradar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tw.gigradar.model.VenueSlot;

public interface VenueSlotRepository extends JpaRepository<VenueSlot, Integer>, JpaSpecificationExecutor<VenueSlot> {}
