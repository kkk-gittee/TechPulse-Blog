#!/bin/bash
# Benchmark: Search API (MySQL full-text search)
# Endpoint: POST /api/search/articles
# Body: {"keyword":"Java","pageNum":1,"pageSize":10}

BASE_URL="${BASE_URL:-http://localhost:8080}"
THREADS="${THREADS:-4}"
CONNECTIONS="${CONNECTIONS:-20}"
DURATION="${DURATION:-30s}"

echo "============================================"
echo "  Benchmark: Search (MySQL Full-Text)"
echo "  POST /api/search/articles"
echo "  Body: {\"keyword\":\"Java\",\"pageNum\":1,\"pageSize\":10}"
echo "============================================"
echo "Target:   $BASE_URL"
echo "Threads:  $THREADS"
echo "Conns:    $CONNECTIONS"
echo "Duration: $DURATION"
echo "--------------------------------------------"

wrk -t$THREADS -c$CONNECTIONS -d$DURATION \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -s /dev/stdin <<'EOF'
wrk.method = "POST"
wrk.body = '{"keyword":"Java","pageNum":1,"pageSize":10}'
EOF
