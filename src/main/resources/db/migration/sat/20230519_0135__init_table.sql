-- Таблица Наземных станция
create table facility
(
    id   serial PRIMARY KEY,
    name text   not null
);

comment on table facility is 'Наземная станция';
comment on column facility.id is 'ID';
comment on column facility.name is 'Имя наземной станции';

-- Таблица создвездий/плеяд спутников
create table area
(
    id   serial PRIMARY KEY,
    name text   not null
);

comment on table area is 'Создвездие/плеяды спутников';
comment on column area.id is 'ID';
comment on column area.name is 'Название создвездия/плеяды спутников';


-- Таблица типов спутника
create table satellite_type
(
    id                  serial PRIMARY KEY,
    name                text   not null,
    total_memory        bigint not null,
    shooting_speed      bigint not null,
    data_transfer_speed bigint not null
);

comment on table satellite_type is 'Типы спутника';
comment on column satellite_type.id is 'ID';
comment on column satellite_type.name is 'Название типа спутника';
comment on column satellite_type.total_memory is 'Общий объем памяти в битах';
comment on column satellite_type.shooting_speed is 'Скорость заполнения памяти в режиме съемки в бит/сек';
comment on column satellite_type.data_transfer_speed is 'Скорость передачи информации в соединения в бит/сек';

INSERT INTO satellite_type (id, name, total_memory, shooting_speed, data_transfer_speed) VALUES ('1', 'KinoSat', '8796093022208', '4294967296', '1073741824');
INSERT INTO satellite_type (id, name, total_memory, shooting_speed, data_transfer_speed) VALUES ('2', 'Zorkiy', '4398046511104', '4294967296', '268435456');


-- Таблица спутников
create table satellite
(
    id                serial PRIMARY KEY,
    name              text   not null,
    satellite_type_id int4   not null references satellite_type (id),
    area_id           int4   references area (id)
);

comment on table satellite is 'Таблица спутников';
comment on column satellite.id is 'ID';
comment on column satellite.name is 'Название спутника';
comment on column satellite.satellite_type_id is 'ID типа спутника';
comment on column satellite.area_id is 'Принадлежность к группе спутников из одной орбиты';

-- Таблица сеансов Наземная станция-Спутник
create table satellite_facility_session
(
    id                 serial PRIMARY KEY,
    satellite_id       int4   not null references satellite (id),
    facility_id        int4   not null references facility (id),
    order_number       int4   not null,
    start_session_time timestamp(2),
    end_session_time   timestamp(2),
    duration           float
);

comment on table satellite_facility_session is 'Таблица сеансов передачи данных Наземная станция-Спутник';
comment on column satellite_facility_session.id is 'ID';
comment on column satellite_facility_session.satellite_id is 'ID спутника';
comment on column satellite_facility_session.facility_id is 'ID Наземная станция';
comment on column satellite_facility_session.order_number is 'Порядковый номер сеанса';
comment on column satellite_facility_session.start_session_time is 'Начало сеанса передачи данных';
comment on column satellite_facility_session.end_session_time is 'Конец сеанса передачи данных';
comment on column satellite_facility_session.duration is 'Продолжительность сеанса передачи данных';

-- Таблица сеансов съемок спутника
create table satellite_area_session
(
    id                 serial PRIMARY KEY,
    satellite_id       int4   not null references satellite (id),
    area_id            int4   not null references area (id),
    order_number       int4   not null,
    start_session_time timestamp(2),
    end_session_time   timestamp(2),
    duration           float
);

comment on table satellite_area_session is 'Таблица сеансов съемок спутника';
comment on column satellite_area_session.id is 'ID';
comment on column satellite_area_session.satellite_id is 'ID спутника';
comment on column satellite_area_session.area_id is 'ID участка территории РФ (созвездиям/плеядам спутников)';
comment on column satellite_area_session.order_number is 'Порядковый номер сеанса';
comment on column satellite_area_session.start_session_time is 'Начало сеанса вхождения на территорию РФ';
comment on column satellite_area_session.end_session_time is 'Конец сеанса вхождения на территорию РФ';
comment on column satellite_area_session.duration is 'Продолжительность сеанса вхождения на территорию РФ';
