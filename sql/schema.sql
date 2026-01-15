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

-- Create policies for profiles table
CREATE POLICY "Users can view own profile" ON profiles
    FOR SELECT USING (auth.uid() = user_id);

-- Policy: Users can view all profiles (for matching/browsing)
CREATE POLICY "Profiles are viewable by all authenticated users"
    ON public.profiles
    FOR SELECT
    TO authenticated
    USING (true);

-- Policy: Users can insert their own profile
CREATE POLICY "Users can insert their own profile"
    ON public.profiles
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

-- Policy: Users can update their own profile
CREATE POLICY "Users can update their own profile"
    ON public.profiles
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Policy: Users can delete their own profile
CREATE POLICY "Users can delete their own profile"
    ON public.profiles
    FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);

-- Create storage bucket for profile images
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'profile-images',
    'profile-images',
    true,
    5242880, -- 5MB in bytes
    ARRAY['image/jpeg', 'image/png', 'image/gif', 'image/webp']
);

-- Enable RLS on storage.objects
ALTER TABLE storage.objects ENABLE ROW LEVEL SECURITY;

-- Policy: Authenticated users can upload to their own folder
CREATE POLICY "Users can upload their own profile images"
    ON storage.objects
    FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id = 'profile-images'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

-- Policy: Authenticated users can view all profile images
CREATE POLICY "Profile images are publicly accessible"
    ON storage.objects
    FOR SELECT
    TO authenticated
    USING (bucket_id = 'profile-images');

-- Policy: Users can update their own images
CREATE POLICY "Users can update their own profile images"
    ON storage.objects
    FOR UPDATE
    TO authenticated
    USING (
        bucket_id = 'profile-images'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

-- Policy: Users can delete their own images
CREATE POLICY "Users can delete their own profile images"
    ON storage.objects
    FOR DELETE
    TO authenticated
    USING (
        bucket_id = 'profile-images'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

-- Matches table
CREATE TABLE IF NOT EXISTS public.matches (
  id uuid not null default gen_random_uuid (),
  user1_id uuid not null,
  user2_id uuid not null,
  matched_at timestamp with time zone null default now(),
  constraint matches_pkey primary key (id),
  constraint unique_match unique (user1_id, user2_id),
  constraint matches_user1_id_fkey foreign KEY (user1_id) references auth.users (id) on delete CASCADE,
  constraint matches_user2_id_fkey foreign KEY (user2_id) references auth.users (id) on delete CASCADE,
  constraint different_users check (user1_id <> user2_id)
) TABLESPACE pg_default;

create index IF not exists idx_matches_user1_id on public.matches using btree (user1_id) TABLESPACE pg_default;
create index IF not exists idx_matches_user2_id on public.matches using btree (user2_id) TABLESPACE pg_default;

ALTER TABLE matches ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own matches"
    ON public.matches
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user1_id OR auth.uid() = user2_id);

-- Messages table
CREATE TABLE IF NOT EXISTS public.messages (
  id uuid not null default gen_random_uuid (),
  match_id uuid not null,
  sender_id uuid not null,
  receiver_id uuid not null,
  content text not null,
  is_read boolean not null default false,
  created_at timestamp with time zone null default now(),
  constraint messages_pkey primary key (id),
  constraint messages_match_id_fkey foreign KEY (match_id) references public.matches (id) on delete CASCADE,
  constraint messages_sender_id_fkey foreign KEY (sender_id) references auth.users (id) on delete CASCADE,
  constraint messages_receiver_id_fkey foreign KEY (receiver_id) references auth.users (id) on delete CASCADE
) TABLESPACE pg_default;

create index IF not exists idx_messages_match_id on public.messages using btree (match_id) TABLESPACE pg_default;
create index IF not exists idx_messages_sender_id on public.messages using btree (sender_id) TABLESPACE pg_default;
create index IF not exists idx_messages_receiver_id on public.messages using btree (receiver_id) TABLESPACE pg_default;
create index IF not exists idx_messages_created_at on public.messages using btree (created_at desc) TABLESPACE pg_default;

ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view messages in their matches"
    ON public.messages
    FOR SELECT
    TO authenticated
    USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

CREATE POLICY "Users can send messages in their matches"
    ON public.messages
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = sender_id);

CREATE POLICY "Users can update their own messages"
    ON public.messages
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

-- Update user_actions policies
CREATE POLICY "Users can view their own actions"
    ON public.user_actions
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can create their own actions"
    ON public.user_actions
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

-- Function to create match when both users like each other
CREATE OR REPLACE FUNCTION create_match_on_mutual_like()
RETURNS TRIGGER AS $$
DECLARE
    mutual_like_exists boolean;
    user1 uuid;
    user2 uuid;
BEGIN
    -- Only proceed if the action is a 'like'
    IF NEW.action = 'like' THEN
        -- Check if the target user has also liked this user
        SELECT EXISTS(
            SELECT 1 FROM user_actions
            WHERE user_id = NEW.target_user_id
            AND target_user_id = NEW.user_id
            AND action = 'like'
        ) INTO mutual_like_exists;
        
        -- If mutual like exists, create a match
        IF mutual_like_exists THEN
            -- Order user IDs consistently (smaller UUID first)
            IF NEW.user_id < NEW.target_user_id THEN
                user1 := NEW.user_id;
                user2 := NEW.target_user_id;
            ELSE
                user1 := NEW.target_user_id;
                user2 := NEW.user_id;
            END IF;
            
            -- Insert match (ignore if already exists)
            INSERT INTO matches (user1_id, user2_id)
            VALUES (user1, user2)
            ON CONFLICT (user1_id, user2_id) DO NOTHING;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for automatic match creation
DROP TRIGGER IF EXISTS create_match_on_like ON user_actions;
CREATE TRIGGER create_match_on_like
AFTER INSERT ON user_actions
FOR EACH ROW
EXECUTE FUNCTION create_match_on_mutual_like();