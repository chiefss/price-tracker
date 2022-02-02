SET FOREIGN_KEY_CHECKS = 0;
truncate table item_price;
truncate table item;
SET FOREIGN_KEY_CHECKS = 1;

insert into item (id, name, url, selector, break_selector, date_from, date_to)
values (1, 'name 1', 'url 1', 'selector 1', 'break selector 1', '2000-01-01 10:10:10', null),
       (2, 'name 2', 'url 2', 'selector 2', 'break selector 2', '2000-01-01 10:10:11', null),
       (3, 'name 3', 'url 3', 'selector 3', 'break selector 3', '2000-01-01 10:10:12', null),
       (4, 'name 4', 'url 4', 'selector 4', 'break selector 4', '2000-01-01 10:10:13', null),
       (5, 'name 5', 'url 5', 'selector 5', 'break selector 5', '2000-01-01 10:10:14', '2000-01-01 10:10:14');

insert into item_price (id, item_id, price, date_from)
values (1, 1, '123.5', '2000-01-01 10:10:10'),
       (2, 1, '123.3', '2000-01-01 10:10:11'),
       (3, 2, '123.5', '2000-01-01 10:10:12'),
       (4, 2, '123.6', '2000-01-01 10:10:13'),
       (5, 2, '123.4', '2000-01-01 10:10:14'),
       (6, 3, '124.4', '2000-01-01 10:10:15'),
       (7, 3, '125.4', '2000-01-01 10:10:16'),
       (8, 3, '126.4', '2000-01-01 10:10:17');
