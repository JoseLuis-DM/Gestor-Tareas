#!/bin/sh
# wait-for.sh

set -e

host="$1"
port="$2"
shift 2

echo "Esperando a $host:$port..."
until nc -z "$host" "$port"; do
  sleep 2
done
echo "¡$host:$port está listo!"

if [ "$1" = "--" ]; then
  shift
fi

exec "$@"
