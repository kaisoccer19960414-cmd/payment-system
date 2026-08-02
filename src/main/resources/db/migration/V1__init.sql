create table users (
                       id bigserial primary key,
                       name varchar(255),
                       balance integer,
                       password varchar(255)
);

create table products (
                          id bigserial primary key,
                          name varchar(255),
                          price integer,
                          stock integer,
                          version bigint
);

create table orders (
                        id bigserial primary key,
                        user_id bigint,
                        total_price integer,
                        ordered_at timestamp
);

create table order_items (
                             id bigserial primary key,
                             order_id bigint references orders(id),
                             product_id bigint,
                             quantity integer,
                             price integer
);