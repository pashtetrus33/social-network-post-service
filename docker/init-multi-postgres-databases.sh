#!/bin/bash

set -e
set -u

function create_databases() {
    database=$1
    schema_name="schema_$(echo $database | sed 's/_db//')"

    echo "Creating database '$database' with schema '$schema_name'"

    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
      DO
      \$\$
      BEGIN
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'postgre_user') THEN
          CREATE USER postgre_user WITH ENCRYPTED PASSWORD 'postgre_secret_password';
        ELSE
          ALTER USER postgre_user WITH ENCRYPTED PASSWORD 'postgre_secret_password';
        END IF;
      END
      \$\$;

      CREATE DATABASE $database;
      GRANT ALL PRIVILEGES ON DATABASE $database TO postgre_user;
EOSQL

    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname="$database" <<-EOSQL
      GRANT ALL ON SCHEMA public TO postgre_user;
      CREATE SCHEMA IF NOT EXISTS $schema_name AUTHORIZATION postgre_user;
      ALTER ROLE postgre_user SET search_path TO $schema_name, public;
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
  echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
  for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
    create_databases $db
  done
  echo "All databases and schemas created successfully!"
fi