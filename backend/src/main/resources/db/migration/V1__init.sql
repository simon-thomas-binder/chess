create table app_user (
                          username varchar(255) primary key,
                          displayname varchar(64) not null,
                          password_hash varchar(255) not null,
                          salt varchar(255) not null,
                          rating int not null default 1200,
                          created_at timestamp not null default now()
);

create table game (
                      id bigserial primary key,
                      white_user_id bigint references app_user(id),
                      black_user_id bigint references app_user(id),
                      fen text not null,
                      status varchar(32) not null,
                      created_at timestamp not null default now()
);

create table move (
                      id bigserial primary key,
                      game_id bigint not null references game(id) on delete cascade,
                      move_no int not null,
                      san varchar(16) not null,
                      from_sq varchar(2) not null,
                      to_sq varchar(2) not null,
                      by_color varchar(5) not null,
                      created_at timestamp not null default now()
);
create index idx_move_game on move(game_id);
