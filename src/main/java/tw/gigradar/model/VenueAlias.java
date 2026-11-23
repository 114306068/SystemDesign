package tw.gigradar.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** D1 場館名稱對應（例：「北岸LIVE」→ 北岸 Live House）。 */
@Entity
@Table(name = "venue_aliases")
public class VenueAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Venue venue;

    @Column(nullable = false, unique = true, length = 150)
    private String alias;

    protected VenueAlias() {}

    public VenueAlias(Venue venue, String alias) {
        this.venue = venue;
        this.alias = alias;
    }

    public Integer getId() { return id; }
    public Venue getVenue() { return venue; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
}
