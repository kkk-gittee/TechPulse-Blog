# TechPulse Blog - API Load Testing

## Prerequisites

- `wrk` installed (macOS: `brew install wrk`, Ubuntu: `apt install wrk`, Windows: use WSL or Docker)
- The blog backend must be running at http://localhost:8080
- At least one article must exist in the database

### Windows users (Docker)

```bash
docker run --rm --network host alpine/wrk -t4 -c20 -d30s http://host.docker.internal:8080/api/article/list?pageNum=1&pageSize=10
```

## Quick Start

```bash
# Run all benchmarks
./run-all.sh

# Run individual endpoint tests
./test-article-list.sh
./test-article-detail.sh
./test-hot-articles.sh
./test-search.sh
```

## What Each Test Measures

| Script | Endpoint | Why It Matters |
|--------|----------|----------------|
| test-article-list | GET /api/article/list | Homepage performance, pagination query |
| test-article-detail | GET /api/article/detail/{id} | Individual page load, Redis view counter |
| test-hot-articles | GET /api/hot/list | Redis sorted set read, cached hot list |
| test-search | POST /api/search/articles | MySQL full-text search performance |

## Metrics Reported

- **Requests/sec**: Throughput under concurrent load
- **Latency (ms)**: Average, standard deviation, max, and percentile distribution (p50, p90, p99)
- **Transfer**: Total data transferred
- **Errors**: Non-2xx responses (should be 0)

## Parameters

Default: 4 threads, 20 connections, 30 seconds duration.
Adjust by editing the variables at the top of each script or via environment variables:

```bash
THREADS=8 CONNECTIONS=50 DURATION=60s ./test-article-list.sh
```
