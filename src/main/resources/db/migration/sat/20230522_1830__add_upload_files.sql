create table uploaded_files (
    id serial PRIMARY KEY,
    file_name text not null
);

comment on table uploaded_files is 'Таблица загруженных файолов';
comment on column satellite_area_session.id is 'ID';
comment on column satellite_area_session.satellite_id is 'Название файла';

