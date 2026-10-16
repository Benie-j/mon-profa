-- Tentatives d'exercices : synchronisation idempotente depuis le stockage local.

create table if not exists public.exercise_attempts (
    id uuid primary key,
    user_id uuid not null references auth.users(id) on delete cascade,
    exercise_id text not null check (length(trim(exercise_id)) > 0),
    subject_id text,
    topic_id text,
    answer_json jsonb not null,
    earned_points integer not null check (earned_points >= 0),
    possible_points integer not null check (possible_points > 0),
    completed_at timestamptz not null,
    created_at timestamptz not null default now(),
    constraint exercise_attempts_score_valid
        check (earned_points <= possible_points)
);

create index if not exists exercise_attempts_user_completed_idx
    on public.exercise_attempts (user_id, completed_at desc);

create index if not exists exercise_attempts_user_subject_idx
    on public.exercise_attempts (user_id, subject_id, completed_at desc);

create index if not exists exercise_attempts_user_topic_idx
    on public.exercise_attempts (user_id, topic_id, completed_at desc);

alter table public.exercise_attempts enable row level security;

drop policy if exists exercise_attempts_select_own
    on public.exercise_attempts;

create policy exercise_attempts_select_own
    on public.exercise_attempts
    for select
    to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists exercise_attempts_insert_own
    on public.exercise_attempts;

create policy exercise_attempts_insert_own
    on public.exercise_attempts
    for insert
    to authenticated
    with check ((select auth.uid()) = user_id);

revoke all on table public.exercise_attempts from anon, authenticated;
grant select, insert on table public.exercise_attempts to authenticated;
