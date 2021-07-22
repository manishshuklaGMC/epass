create table account (id bigserial primary key, name text not null, identifier text not null, passwordhashed text not null, created_at timestamp with time zone default now(), account_identifier_type text, account_type text, organizationid text, status text);

alter table account add constraint identifier_unique unique (identifier);

create table application (id bigserial primary key, purpose text not null, type text not null, status text not null, issuer_id bigint, start_time timestamp with time zone, end_time timestamp with time zone, created_at timestamp with time zone default now(), token text, entity text, orderid text, partner_company text, valid_locations text);

create table orders (id bigserial primary key, request_count integer not null, created_at timestamp with time zone default now(), updated_at timestamp with time zone, zip_file_url text, status text not null, account_id bigint, uuid text, type text, pdf_url text, reason text, purpose text, validtill timestamp with time zone default now() + INTERVAL '7 day');

create table organization (id bigserial primary key, name text not null, created_at timestamp with time zone default now(), orgid text, status text, active_pass_limit int default 5000);

alter table organization add constraint org_unique unique (orgid);

create table otp (id bigserial primary key, created_at timestamp with time zone default now(), valid_till timestamp with time zone DEFAULT now() + INTERVAL '1 hour', session_id bigint, otp text not null, public_key text, identifier text, identifier_type text, try_count integer, status text default 'unverified');

create table session(id bigserial primary key, created_at timestamp with time zone default now(), valid_till timestamp with time zone DEFAULT now() + INTERVAL '1 day', auth_token text, session_status text, user_id bigint);

create table token_approver(id bigserial primary key, identifier text not null, identifier_type text not null, org_name text not null);


create index on orders (uuid);
create index on orders (created_at);
create index on orders (account_id);
create index on orders (status);
create index on account(organizationid);
create index on application (token);
create index on application (orderid);

alter table account add column state_id int default 1;

alter table organization add column state_id int default 1;

create table state(id bigserial primary key, name text not null, config json default '{}', created_at timestamp with time zone default now());

alter table organization drop constraint org_unique;

alter table organization add constraint composite_orgid_state unique(orgid, state_id);

alter table account drop constraint identifier_unique;

alter table account add constraint state_identifier_unique unique(identifier, state_id);

