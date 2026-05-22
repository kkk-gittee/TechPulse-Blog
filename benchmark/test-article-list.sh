#!/bin/bash
# Benchmark: Article List API (homepage pagination)
# Endpoint: GET /api/article/list?pageNum=1&pageSize=10

BASE_URL="${BASE_URL:-http://localhost:8080}"
THREADS="${THREADS:-4}"
CONNECTIONS="${CONNECTIONS:-20}"
DURATION="${DURATION:-30s}"

echo "============================================"
echo "  Benchmark: Article List"
echo "  GET /api/article/list?pageNum=1&pageSize=10"
echo "============================================"
echo "Target:   $BASE_URL"
echo "Threads:  $THREADS"
echo "Conns:    $CONNECTIONS"
echo "Duration: $DURATION"
echo "--------------------------------------------"

wrk -t$THREADS -c$CONNECTIONS -d$DURATION \
  -H "Accept: application/json" \
  "$BASE_URL/api/article/list?pageNum=1&pageSize=10"
