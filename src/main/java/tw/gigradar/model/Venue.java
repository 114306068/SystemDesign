package tw.gigradar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import tw.gigradar.model.enums.ClaimStatus;

/** D1 場館。階段一由爬蟲建立（unclaimed），階段二由場地方認領、管理者審核後自行管理檔期。 */
@Entity
@Table(name = "venues", indexes = @Index(name = "ix_venues_district", columnList = "district"))
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    /** 行政區，例：大安區 */
    @Column(nullable = false, length = 20)
    private String district;

    @Column(length = 255)
    private String address;

    private Integer capacity;

    @Column(length = 500)
    private String officialUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus claimStatus = ClaimStatus.unclaimed;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VenueAlias> aliases = new ArrayList<>();

    protected Venue() {}

    public Venue(String name, String district) {
        this.name = name;
        this.district = district;
    }

    /** 加一個別名（1.2 整理資料格式：各來源對同一場館的寫法不同）。 */
    public Venue addAlias(String alias) {
        aliases.add(new VenueAlias(this, alias));
        return this;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public ClaimStatus getClaimStatus() { return claimStatus; }
    public void setClaimStatus(ClaimStatus claimStatus) { this.claimStatus = claimStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<VenueAlias> getAliases() { return aliases; }
}
