package org.devel.pricetracker.application.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "item")
public class Item extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "url", nullable = false, length = 4096)
    private String url;

    @Column(name = "selector", nullable = false, length = 1024)
    private String selector;

    @Column(name = "break_selector", nullable = false, length = 1024)
    private String breakSelector;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Item(String name, String url, String selector, String breakSelector) {
        this.name = name;
        this.url = url;
        this.selector = selector;
        this.breakSelector = breakSelector;
    }
}
