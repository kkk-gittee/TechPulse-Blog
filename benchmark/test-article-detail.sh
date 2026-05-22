#!/bin/bash
# Benchmark: Article Detail API
# Endpoint: GET /api/article/detail/{id}
# NOTE: Article ID 1 is used as default. Set ARTICLE_ID env var to override.

BASE_URL="${BASE_URL:-http://localhost:8080}"
ARTICLE_ID="${ARTICLE_ID:-1}"
THREADS="${THREADS:-4}"
CONNECTIONS="${CONNECTIONS:-20}"
DURATION="${DURATION:-30s}"

echo "============================================"
echo "  Benchmark: Article Detail"
echo "  GET /api/article/detail/$ARTICLE_ID"
echo "============================================"
echo "Target:   $BASE_URL"
echo "Threads:  $THREADS"
echo "Conns:    $CONNECTIONS"
echo "Duration: $DURATION"
echo "--------------------------------------------"

wrk -t$THREADS -c$CONNECTIONS -d$DURATION \
  -H "Accept: application/json" \
  "$BASE_URL/api/article/detail/$ARTICLE_ID"
