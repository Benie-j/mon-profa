-- KALYNO : série de régularité quotidienne
-- L'activité est comptée à l'ouverture du tableau de bord.
-- La date est calculée côté serveur en UTC, jamais depuis l'horloge du téléphone.

create table if not exists public.user_streaks (
    user_id uuid primary key references auth.users(id) on delete cascade,
    current_streak integer not null default 0 check (current_streak >= 0),
    longest_streak integer not null default 0 check (longest_streak >= current_streak),
    last_activity_date date,
    updated_at timestamptz not null default now()
);

alter table public.user_streaks enable row level security;

drop policy if exists user_streaks_select_own on public.user_streaks;
create policy user_streaks_select_own
    on public.user_streaks
    for select
    to authenticated
    using (auth.uid() = user_id);

revoke all on table public.user_streaks from anon, authenticated;
grant select on table public.user_streaks to authenticated;

create or replace function public.register_daily_streak_activity()
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_today date := (now() at time zone 'UTC')::date;
    v_row public.user_streaks%rowtype;
begin
    if v_user_id is null then
        raise exception 'Authentication required'
            using errcode = '28000';
    end if;

    insert into public.user_streaks (
        user_id, current_streak, longest_streak, last_activity_date
    )
    values (v_user_id, 1, 1, v_today)
    on conflict (user_id) do nothing;

    select *
      into v_row
      from public.user_streaks
     where user_id = v_user_id
     for update;

    if v_row.last_activity_date is distinct from v_today then
        if v_row.last_activity_date = v_today - 1 then
            v_row.current_streak := v_row.current_streak + 1;
        else
            v_row.current_streak := 1;
        end if;

        v_row.longest_streak := greatest(v_row.longest_streak, v_row.current_streak);
        v_row.last_activity_date := v_today;
        v_row.updated_at := now();

        update public.user_streaks
           set current_streak = v_row.current_streak,
               longest_streak = v_row.longest_streak,
               last_activity_date = v_row.last_activity_date,
               updated_at = v_row.updated_at
         where user_id = v_user_id;
    end if;

    return jsonb_build_object(
        'current_streak', v_row.current_streak,
        'longest_streak', v_row.longest_streak,
        'last_activity_date', v_row.last_activity_date
    );
end;
$$;

revoke all on function public.register_daily_streak_activity() from public, anon;
grant execute on function public.register_daily_streak_activity() to authenticated;
