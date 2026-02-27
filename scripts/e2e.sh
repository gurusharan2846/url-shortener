#!/bin/sh
set -eu

echo "== api health =="
curl -fsS http://api:8080/actuator/health >/dev/null

echo "== analytics health =="
curl -fsS http://analytics:8082/actuator/health >/dev/null

echo "== shorten =="
JSON_PAYLOAD='{"url":"https://apple.com"}'
RESP="$(curl -fsS -X POST http://api:8080/shorten \
  -H "Content-Type: application/json" \
  -d "$JSON_PAYLOAD")"
echo "$RESP"

case "$RESP" in
  *\"code\"*)
    CODE="$(echo "$RESP" | sed -n 's/.*"code"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
    ;;
  *)
    CODE="$(echo "$RESP" | tr -d '\r\n' | sed 's/^[[:space:]]*//; s/[[:space:]]*$//')"
    ;;
esac

echo "code=$CODE"

echo "== redirect hit =="
curl -fsS -L -o /dev/null "http://redirect:8081/${CODE}"

echo "PASS"