#!/bin/bash
# Run all benchmarks sequentially

echo "============================================"
echo "  TechPulse Blog - Full Benchmark Suite"
echo "============================================"
echo ""

BASE_URL="${BASE_URL:-http://localhost:8080}"

# Health check first
echo "Checking if backend is reachable..."
if ! curl -sf "$BASE_URL/api/category/list" > /dev/null 2>&1; then
    echo "[ERROR] Backend is not reachable at $BASE_URL"
    echo "        Please start the backend first."
    exit 1
fi
echo "Backend is up."
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

for script in test-article-list.sh test-article-detail.sh test-hot-articles.sh test-search.sh; do
    echo ""
    bash "$SCRIPT_DIR/$script"
    echo ""
    echo "--------------------------------------------"
    echo ""
done

echo "============================================"
echo "  All benchmarks completed."
echo "============================================"
