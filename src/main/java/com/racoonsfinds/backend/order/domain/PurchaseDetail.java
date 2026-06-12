package com.racoonsfinds.backend.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_details")
@Getter @Setter
public class PurchaseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto", precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "amount")
    private Integer amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "purchase_details_ibfk_1"))
    private Purchase purchase;

    // Snapshot del producto en el momento de la compra (sin FK JPA hacia catalog).
    // Las órdenes capturan datos point-in-time: sobreviven aunque el producto cambie o se borre.
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "seller_id")
    private Long sellerId;
}
