set search_path to sat;

alter table satellite_area_session
    add column init_mem_status float;

alter table satellite_area_session
    add column fin_mem_status float;
