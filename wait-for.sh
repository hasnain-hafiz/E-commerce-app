#!/bin/sh
# wait-for.sh

set -e

host="$1"
port="$2"
shift 2
cmd="$@"

until nc -z "$host" "$port"; do
  echo "Waiting for $host:$port..."
  sleep 2
done

echo "$host:$port is up!"
exec $cmd
