#!/bin/bash
host="$1"
shift
until mysql -h "$host" -uappuser -papppass -e "select 1" &> /dev/null; do
  echo "Waiting for MySQL at $host..."
  sleep 2
done
exec "$@"
