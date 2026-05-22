#!/bin/bash
# Benchmark: Hot Articles API (Redis sorted set read)
# Endpoint: GET /api/hot/list?limit=10

BASE_URL="${BASE_URL:-http://localhost:8080}"
THREADS="${THREADS:-4}"
CONNECTIONS="${CONNECTIONS:-20}"
DURATION="${DURATION:-30s}"

echo "============================================"
echo "  Benchmark: Hot Articles (Redis-backed)"
echo "  GET /api/hot/list?limit=10"
echo "============================================"
echo "Target:   $BASE_URL"
echo "Threads:  $THREADS"
echo "Conns:    $CONNECTIONS"
echo "Duration: $DURATION"
echo "--------------------------------------------"

wrk -t$THREADS -c$CONNECTIONS -d$DURATION \
  -H "Accept: application/json" \
  "$BASE_URL/api/hot/list?limit=10"
