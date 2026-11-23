package tw.gigradar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

/** D1 表演者（樂團）。爬蟲建立時 user 為空，表演者註冊後可認領（一個帳號只能認領一團）。 */
@Entity
@Table(name = "performers", indexes = @Index(name = "ix_performers_genre", columnList = "genre"))
public class Performer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 50)
    private String genre;

    @Lob
    private String bio;

    @Column(length = 500)
    private String socialUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "performer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformerAlias> aliases = new ArrayList<>();

    protected Performer() {}

    public Performer(String name, String genre) {
        this.name = name;
        this.genre = genre;
    }

    /** 加一個別名（中英文、縮寫），爬蟲比對名稱時用。 */
    public Performer addAlias(String alias) {
        aliases.add(new PerformerAlias(this, alias));
        return this;
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getSocialUrl() { return socialUrl; }
    public void setSocialUrl(String socialUrl) { this.socialUrl = socialUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<PerformerAlias> getAliases() { return aliases; }
}
