--- SQL Schema for CampusConnect Application

CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS public.profiles (
  id uuid not null default gen_random_uuid (),
  user_id uuid not null,
  first_name text not null,
  last_name text not null,
  birthdate date not null,
  gender text not null,
  pronouns text not null,
  degree_type text not null,
  semester integer not null,
  study_program text not null,
  looking_for text not null,
  interested_in text not null,
  bio text not null,
  interests text[] not null default '{}'::text[],
  image_urls text[] not null default '{}'::text[],
  created_at timestamp with time zone null default now(),
  updated_at timestamp with time zone null default now(),
  constraint profiles_pkey primary key (id),
  constraint unique_user_profile unique (user_id),
  constraint profiles_user_id_fkey foreign KEY (user_id) references auth.users (id) on delete CASCADE,
  constraint check_semester_master check (
    (
      (degree_type <> 'Master'::text)
      or (
        (semester >= 1)
        and (semester <= 4)
      )
    )
  ),
  constraint check_max_images check (
    (
      (array_length(image_urls, 1) is null)
      or (array_length(image_urls, 1) <= 4)
    )
  ),
  constraint check_min_interests check (
    (
      (array_length(interests, 1) is null)
      or (array_length(interests, 1) >= 3)
    )
  ),
  constraint check_semester_bachelor check (
    (
      (degree_type <> 'Bachelor'::text)
      or (
        (semester >= 1)
        and (semester <= 6)
      )
    )
  )
) TABLESPACE pg_default;

create index IF not exists idx_profiles_user_id on public.profiles using btree (user_id) TABLESPACE pg_default;

DROP TRIGGER IF EXISTS set_updated_at ON profiles;
CREATE TRIGGER set_updated_at BEFORE UPDATE ON profiles 
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS public.user_actions (
  id uuid not null default gen_random_uuid (),
  user_id uuid not null,
  target_user_id uuid not null,
  action text not null,
  created_at timestamp with time zone null default now(),
  constraint user_actions_pkey primary key (id),
  constraint unique_user_action unique (user_id, target_user_id),
  constraint user_actions_user_id_fkey foreign KEY (user_id) references auth.users (id) on delete CASCADE,
  constraint user_actions_target_user_id_fkey foreign KEY (target_user_id) references auth.users (id) on delete CASCADE,
  constraint user_actions_action_check check (
    (action = any (array['like'::text, 'pass'::text]))
  )
) TABLESPACE pg_default;

create index IF not exists idx_user_actions_user_id on public.user_actions using btree (user_id) TABLESPACE pg_default;

create index IF not exists idx_user_actions_target_user_id on public.user_actions using btree (target_user_id) TABLESPACE pg_default;

create index IF not exists idx_user_actions_action on public.user_actions using btree (action) TABLESPACE pg_default;

create index IF not exists idx_user_actions_match_check on public.user_actions using btree (user_id, target_user_id, action) TABLESPACE pg_default;


-- Enable RLS
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_actions ENABLE ROW LEVEL SECURITY;

-- Example policy for profiles
CREATE POLICY "Users can view own profile" ON profiles
    FOR SELECT USING (auth.uid() = user_id);