package tw.gigradar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import tw.gigradar.model.enums.Fulfillment;
import tw.gigradar.model.enums.OrderStatus;

/** D6 預購訂單（主檔）。買了哪些商品記在 {@link OrderItem}（明細）。 */
@Entity
@Table(name = "orders") // ORDER 是 SQL 保留字，所以表名用複數
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 賣方 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performer_id", nullable = false)
    private Performer performer;

    /** 取貨場次 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Fulfillment fulfillment = Fulfillment.pickup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.pending_payment;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal totalAmount;

    /** 取貨憑證 */
    @Column(unique = true, length = 20)
    private String pickupCode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime pickedUpAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}

    public Order(User user, Performer performer, Event event, BigDecimal totalAmount) {
        this.user = user;
        this.performer = performer;
        this.event = event;
        this.totalAmount = totalAmount;
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public Performer getPerformer() { return performer; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public Fulfillment getFulfillment() { return fulfillment; }
    public void setFulfillment(Fulfillment fulfillment) { this.fulfillment = fulfillment; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public List<OrderItem> getItems() { return items; }
}
