package com.devel.pricetracker.application.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Item", indexes = {
    @Index(name = "uq_item_url_idx", columnList = "url", unique = true)
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "url", nullable = false, length = 4096)
    private String url;

    @Column(name = "selector", nullable = false, length = 1024)
    private String selector;

    @Column(name = "break_selector", nullable = false, length = 1024)
    @ColumnDefault("")
    private String breakSelector;

    @Column(name = "date_from", nullable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateFrom;

    @Column(name = "date_to", nullable = true, columnDefinition="TIMESTAMP DEFAULT NULL NULL")
    private LocalDateTime dateTo;
}
