package com.devel.pricetracker.application.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ItemPrice", indexes = {
    @Index(name = "ItemPrice_item_idx", columnList = "item_id"),
    @Index(name = "uq_ItemPrice_item_date_idx", columnList = "item_id, date_from", unique = true)
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ItemPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="item_id")
    private ItemEntity item;

    @Column(name = "price", nullable = false)
    private Float price;

    @Column(name = "date_from", nullable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateFrom;
}
