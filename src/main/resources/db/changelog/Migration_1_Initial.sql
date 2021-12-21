--liquibase formatted sql

--changeset master:1

CREATE TABLE item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    url TEXT NOT NULL,
    selector TEXT NOT NULL,
    break_selector TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uq_item_url_idx ON Item (url);

CREATE TABLE item_price (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL,
    price REAL NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (item_id) REFERENCES item(id)
);

CREATE INDEX ItemPrice_item_idx ON item_price (item_id);
CREATE UNIQUE INDEX uq_ItemPrice_item_date_idx ON item_price (item_id, created_at);

-- rollback