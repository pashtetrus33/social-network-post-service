#!/bin/sh

set -e  # Прерывать выполнение при ошибках
set -u  # Ошибка при попытке использовать неинициализированную переменную

# Загружаем переменные из .env, если файл существует
if [ -f .env ]; then
    export `grep -v '^#' .env | xargs`
fi

# Функция создания базы данных и схемы
create_databases() {
    database=$1
    schema_name="schema_`echo $database | sed 's/_db//'`"

    echo "Создаю базу данных '$database' с схемой '$schema_name'"

    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<EOF
      DO
      \$\$
      BEGIN
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'postgre_user') THEN
          CREATE USER postgre_user WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';
        ELSE
          ALTER USER postgre_user WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';
        END IF;
      END
      \$\$;

      CREATE DATABASE $database;
      GRANT ALL PRIVILEGES ON DATABASE $database TO postgre_user;
EOF

    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname="$database" <<EOF
      GRANT ALL ON SCHEMA public TO postgre_user;
      CREATE SCHEMA IF NOT EXISTS $schema_name AUTHORIZATION postgre_user;
      ALTER ROLE postgre_user SET search_path TO $schema_name, public;
EOF
}

# Проверяем, есть ли список баз в переменной POSTGRES_MULTIPLE_DATABASES
if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
  echo "Запрос на создание нескольких баз данных: $POSTGRES_MULTIPLE_DATABASES"
  for db in `echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '`; do
    create_databases "$db"
  done
  echo "Все базы данных и схемы успешно созданы!"
else
  echo "Переменная POSTGRES_MULTIPLE_DATABASES не задана, создание баз данных пропущено."
fi