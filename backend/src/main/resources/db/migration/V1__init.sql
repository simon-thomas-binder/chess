create type color as enum ('WHITE','BLACK');
create type user_type as enum ('HUMAN','BOT');
create type game_status as enum ('PENDING','ACTIVE','FINISHED','ABORTED');
create type piece_type as enum ('KING','QUEEN','ROOK','BISHOP','KNIGHT','PAWN');
create type move_flag as enum ('NORMAL','CAPTURE','CASTLE_KING','CASTLE_QUEEN','PROMOTION','EN_PASSANT');
create type game_end_flag as enum ('CHECKMATE','TIMEOUT','RESIGNATION','STALEMATE','FORFEIT');

create table app_user (
                          username varchar(255) primary key,
                          displayname varchar(64) not null,
                          password_hash varchar(255) not null,
                          salt varchar(255) not null,
                          rating int not null default 1200,
                          type user_type not null default 'HUMAN',
                          created_at timestamptz not null default now()
);

create table game (
                      id bigserial primary key,
                      status game_status not null default 'PENDING',
                      board_width int not null default 8,
                      board_height int not null default 8,
                      initial_time_ms int not null default 300000,
                      increment_ms int not null default 0,
                      delay_ms int,
                      winner color,
                      end_game_flag game_end_flag,
                      end_time timestamptz NULL,
                      created_at timestamptz not null default now()
);

create table game_participant (
                                  id bigserial primary key,
                                  game_id bigint not null references game(id) on delete cascade,
                                  username varchar(255) not null references app_user(username),
                                  color color not null,
                                  rating_snapshot int,
                                  unique (game_id, color)
);

create table move (
                      id bigserial primary key,
                      game_id bigint not null references game(id) on delete cascade,
                      ply int not null,
                      actor color not null,
                      piece piece_type not null,
                      from_x int not null,
                      from_y int not null,
                      to_x int not null,
                      to_y int not null,
                      flag move_flag not null default 'NORMAL',
                      promotion_to piece_type,
                      actor_time_left_ms_after bigint,
                      server_received_at timestamptz not null default now(),
                      unique (game_id, ply)
);
create index idx_move_game on move(game_id);
