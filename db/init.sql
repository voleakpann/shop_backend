-- Creates the databases the services expect, inside the single Postgres
-- container. Runs automatically the first time the postgres volume is created.
CREATE DATABASE ministore_auth;
CREATE DATABASE ministore_products;
CREATE DATABASE ministore_orders;
CREATE DATABASE ministore_comments;
CREATE DATABASE ministore_blog;
