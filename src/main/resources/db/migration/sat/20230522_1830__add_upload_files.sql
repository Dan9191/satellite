set search_path to sat;

create table uploaded_files (
    id serial PRIMARY KEY,
    file_name text not null
)

