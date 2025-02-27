
-- Create the table
create table countries (
  id bigint primary key generated always as identity,
  name text not null
);
-- Insert some sample data into the table
insert into countries (name)
values
  ('Canada'),
  ('United States'),
  ('Mexico');
