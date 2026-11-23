-- Gig Radar 演出雷達 資料庫結構（由 export-schema 工作自動產生，請勿手改）
CREATE DATABASE IF NOT EXISTS gig_radar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gig_radar;

    create table commission_rules (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        effective_from date not null,
        rate decimal(5,4) not null,
        rule_type enum ('match','merch') not null,
        created_by integer,
        primary key (id)
    ) engine=InnoDB;

    create table crawl_runs (
        id integer not null auto_increment,
        fetched_count integer not null,
        finished_at datetime(6),
        inserted_count integer not null,
        merged_count integer not null,
        message longtext,
        started_at datetime(6) not null,
        status enum ('anomaly','failed','success'),
        source_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table crawl_sources (
        id integer not null auto_increment,
        is_active bit not null,
        base_url varchar(500),
        expected_min_count integer not null,
        `module` varchar(100) not null,
        name varchar(100) not null,
        primary key (id)
    ) engine=InnoDB;

    create table event_performers (
        event_id integer not null,
        performer_id integer not null
    ) engine=InnoDB;

    create table events (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        description longtext,
        event_date date not null,
        genre varchar(50),
        source_type enum ('crawler','match','performer') not null,
        source_url varchar(500),
        start_time time(6),
        ticket_url varchar(500),
        title varchar(255) not null,
        updated_at datetime(6) not null,
        crawl_source_id integer,
        venue_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table follows (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        genre varchar(50),
        target_type enum ('genre','performer','venue') not null,
        performer_id integer,
        user_id integer not null,
        venue_id integer,
        primary key (id),
        constraint ck_follow_target check ((target_type = 'performer' AND performer_id IS NOT NULL) OR (target_type = 'venue' AND venue_id IS NOT NULL) OR (target_type = 'genre' AND genre IS NOT NULL))
    ) engine=InnoDB;

    create table match_requests (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        decided_at datetime(6),
        final_rent decimal(10,0),
        message longtext,
        status enum ('accepted','cancelled','pending','rejected') not null,
        performer_id integer not null,
        slot_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table notifications (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        emailed_at datetime(6),
        message varchar(255) not null,
        is_read bit not null,
        event_id integer not null,
        user_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table order_items (
        id integer not null auto_increment,
        quantity integer not null,
        unit_price decimal(10,0) not null,
        order_id integer not null,
        product_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table orders (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        fulfillment enum ('pickup','shipping') not null,
        picked_up_at datetime(6),
        pickup_code varchar(20),
        status enum ('cancelled','paid','pending_payment','picked_up') not null,
        total_amount decimal(10,0) not null,
        event_id integer,
        performer_id integer not null,
        user_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table performer_aliases (
        id integer not null auto_increment,
        alias varchar(150) not null,
        performer_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table performers (
        id integer not null auto_increment,
        bio longtext,
        created_at datetime(6) not null,
        genre varchar(50),
        name varchar(150) not null,
        social_url varchar(500),
        user_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table products (
        id integer not null auto_increment,
        is_active bit not null,
        created_at datetime(6) not null,
        description longtext,
        image_url varchar(500),
        name varchar(150) not null,
        price decimal(10,0) not null,
        stock integer not null,
        performer_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table promotions (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        end_date date not null,
        fee decimal(10,0) not null,
        start_date date not null,
        event_id integer,
        performer_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table transactions (
        id integer not null auto_increment,
        commission_amount decimal(10,0) not null,
        commission_rate decimal(5,4),
        created_at datetime(6) not null,
        gross_amount decimal(10,0) not null,
        paid_at datetime(6),
        payment_ref varchar(100),
        payment_status enum ('failed','paid','pending') not null,
        txn_type enum ('match_commission','merch_commission','promotion') not null,
        match_request_id integer,
        order_id integer,
        promotion_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table users (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        display_name varchar(100) not null,
        email varchar(255) not null,
        password_hash varchar(255) not null,
        role enum ('admin','audience','performer','venue') not null,
        primary key (id)
    ) engine=InnoDB;

    create table venue_aliases (
        id integer not null auto_increment,
        alias varchar(150) not null,
        venue_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table venue_slots (
        id integer not null auto_increment,
        allow_co_performance bit not null,
        apply_deadline datetime(6),
        created_at datetime(6) not null,
        end_time time(6) not null,
        last_minute_rent decimal(10,0),
        max_performers integer not null,
        rent decimal(10,0) not null,
        slot_date date not null,
        start_time time(6) not null,
        status enum ('booked','closed','open') not null,
        event_id integer,
        venue_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table venues (
        id integer not null auto_increment,
        address varchar(255),
        capacity integer,
        claim_status enum ('approved','pending','rejected','unclaimed') not null,
        created_at datetime(6) not null,
        district varchar(20) not null,
        name varchar(150) not null,
        official_url varchar(500),
        owner_user_id integer,
        primary key (id)
    ) engine=InnoDB;

    alter table crawl_sources 
       add constraint UKpvhd9vb3fmveegxdr92h1hrn6 unique (name);

    create index ix_events_event_date 
       on events (event_date);

    create index ix_events_genre 
       on events (genre);

    alter table events 
       add constraint uq_event_dedup unique (venue_id, event_date, start_time);

    alter table match_requests 
       add constraint uq_match_once unique (slot_id, performer_id);

    alter table notifications 
       add constraint uq_notify_once unique (user_id, event_id);

    alter table orders 
       add constraint UK9epvsdbn94lefs6fk6ugajpl9 unique (pickup_code);

    alter table performer_aliases 
       add constraint UK9t3g17lms1al1vhrn7hnk6qor unique (alias);

    create index ix_performers_genre 
       on performers (genre);

    alter table performers 
       add constraint UKmug0a2bhm5stvtcivb6st6mwu unique (name);

    alter table performers 
       add constraint UKe8na53oeomxmo2h5e7ls820xm unique (user_id);

    create index ix_transactions_txn_type 
       on transactions (txn_type);

    alter table users 
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table venue_aliases 
       add constraint UK2p6nm4kcr1fnfbsxuu7bdsit7 unique (alias);

    create index ix_venue_slots_slot_date 
       on venue_slots (slot_date);

    alter table venue_slots 
       add constraint uq_slot unique (venue_id, slot_date, start_time);

    create index ix_venues_district 
       on venues (district);

    alter table venues 
       add constraint UKc2om8hy3unm9k7dxwddow2rv1 unique (name);

    alter table commission_rules 
       add constraint FKnn8hh3e8fd8sj8em98p2xpde2 
       foreign key (created_by) 
       references users (id);

    alter table crawl_runs 
       add constraint FKmkpwg0q69rhomwk13i7ie40t3 
       foreign key (source_id) 
       references crawl_sources (id);

    alter table event_performers 
       add constraint FK4wy50iehw59havjqghrqiai52 
       foreign key (performer_id) 
       references performers (id);

    alter table event_performers 
       add constraint FKhh0agpeefrpap7hf71hjlpju6 
       foreign key (event_id) 
       references events (id);

    alter table events 
       add constraint FKpcbdhr54ll9eue7x5dvych323 
       foreign key (crawl_source_id) 
       references crawl_sources (id);

    alter table events 
       add constraint FKqdxygdernwwt74hdvix9u5nr3 
       foreign key (venue_id) 
       references venues (id);

    alter table follows 
       add constraint FK5uo9ynjyb1mixd2nx35yoxpqh 
       foreign key (performer_id) 
       references performers (id) 
       on delete cascade;

    alter table follows 
       add constraint FKn4am7c82j2uo8pkw4x7qibf12 
       foreign key (user_id) 
       references users (id) 
       on delete cascade;

    alter table follows 
       add constraint FKje1d54qwif4n1vui4wdsu39n0 
       foreign key (venue_id) 
       references venues (id) 
       on delete cascade;

    alter table match_requests 
       add constraint FK8eiomodtqwhv90hc70kvi03fl 
       foreign key (performer_id) 
       references performers (id);

    alter table match_requests 
       add constraint FK53k3ot9g8t09licgt3ucj8pij 
       foreign key (slot_id) 
       references venue_slots (id);

    alter table notifications 
       add constraint FKkau4auenkuj1m86s9mydmets 
       foreign key (event_id) 
       references events (id) 
       on delete cascade;

    alter table notifications 
       add constraint FK9y21adhxn0ayjhfocscqox7bh 
       foreign key (user_id) 
       references users (id) 
       on delete cascade;

    alter table order_items 
       add constraint FKbioxgbv59vetrxe0ejfubep1w 
       foreign key (order_id) 
       references orders (id) 
       on delete cascade;

    alter table order_items 
       add constraint FKocimc7dtr037rh4ls4l95nlfi 
       foreign key (product_id) 
       references products (id);

    alter table orders 
       add constraint FK43g2yroy6l7lfomw37wajkqrn 
       foreign key (event_id) 
       references events (id);

    alter table orders 
       add constraint FK2bhl22yo23a76il4xerstxs9d 
       foreign key (performer_id) 
       references performers (id);

    alter table orders 
       add constraint FK32ql8ubntj5uh44ph9659tiih 
       foreign key (user_id) 
       references users (id);

    alter table performer_aliases 
       add constraint FKmodwecg6msr0mrhdf0ibtu0af 
       foreign key (performer_id) 
       references performers (id) 
       on delete cascade;

    alter table performers 
       add constraint FK2qad3uv2caxp5m7eq2wegl0vb 
       foreign key (user_id) 
       references users (id);

    alter table products 
       add constraint FK53wa0jati480mtfjl7xwa88e1 
       foreign key (performer_id) 
       references performers (id);

    alter table promotions 
       add constraint FKnqjooeh8y4glf87c0yf2qeo1g 
       foreign key (event_id) 
       references events (id);

    alter table promotions 
       add constraint FK61ken53gyv1ikqq4ov1uurlw 
       foreign key (performer_id) 
       references performers (id);

    alter table transactions 
       add constraint FK4ssynlpini6bkbr50dwwvfrcu 
       foreign key (match_request_id) 
       references match_requests (id);

    alter table transactions 
       add constraint FKfyxndk58yiq2vpn0yd4m09kbt 
       foreign key (order_id) 
       references orders (id);

    alter table transactions 
       add constraint FKgje9siaym6teookgssk58rjqf 
       foreign key (promotion_id) 
       references promotions (id);

    alter table venue_aliases 
       add constraint FKft12e38t058d06h2w8x7fgpyu 
       foreign key (venue_id) 
       references venues (id) 
       on delete cascade;

    alter table venue_slots 
       add constraint FKb8wxqnbdkn7t7xut1kdbrwv4i 
       foreign key (event_id) 
       references events (id);

    alter table venue_slots 
       add constraint FKgos1r977giy112j1nnf0ms58t 
       foreign key (venue_id) 
       references venues (id);

    alter table venues 
       add constraint FKsp3trwl3exf53pljplbomgp1q 
       foreign key (owner_user_id) 
       references users (id);
