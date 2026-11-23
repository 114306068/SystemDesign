package tw.gigradar.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** D1 樂團別名表：中英文、縮寫都對應到同一個樂團（爬蟲 1.3 比對用）。 */
@Entity
@Table(name = "performer_aliases")
public class PerformerAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Performer performer;

    @Column(nullable = false, unique = true, length = 150)
    private String alias;

    protected PerformerAlias() {}

    public PerformerAlias(Performer performer, String alias) {
        this.performer = performer;
        this.alias = alias;
    }

    public Integer getId() { return id; }
    public Performer getPerformer() { return performer; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
}
