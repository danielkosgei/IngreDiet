CREATE TABLE IF NOT EXISTS "profile" (
	"id" uuid PRIMARY KEY NOT NULL,
	"first_name" text NOT NULL,
	"last_name" text NOT NULL,
	"email" text NOT NULL
);

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

ALTER TABLE "profile" ENABLE ROW LEVEL SECURITY;