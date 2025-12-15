# K6 Load Testing Documentation

This directory contains K6 load tests for the Nordic Electronics API.

## Test Types

### 1. Load Test (`product_load_test.js`)
- **Purpose**: Tests system performance under steady, sustained load
- **Pattern**: Ramp up to 50 VUs, maintain for 2 minutes, then ramp down
- **Use Case**: Validate system can handle expected production load

### 2. Spike Test (`product_spike_test.js`)
- **Purpose**: Tests system behavior during sudden traffic surges
- **Pattern**: Normal load → sudden spike to 500 VUs → rapid drop → recovery
- **Use Case**: Validate system can handle traffic spikes (e.g., flash sales, viral content)

### 3. Stress Test (`product_stress_test.js`)
- **Purpose**: Finds the breaking point of the system
- **Pattern**: Gradual ramp-up from 10 to 500 VUs
- **Use Case**: Determine maximum capacity and identify bottlenecks

## Running Tests

### Prerequisites
1. Ensure Docker Compose services are running:
   ```bash
   docker compose up -d
   ```

2. Wait for Spring application to be ready (check health endpoint)

### Execute Tests

```bash
# Load Test
docker compose run --rm k6-load

# Spike Test
docker compose run --rm k6-spike

# Stress Test
docker compose run --rm k6-stress
```

## Test Endpoints

All tests use weighted distribution across these endpoints:

- `/api/postgresql/products/best-reviewed` (30% weight)
- `/api/postgresql/products/best-selling` (30% weight)
- `/api/postgresql/products/paginated?page=0&size=24&sortBy=name&sortDirection=asc` (20% weight)
- `/api/postgresql/categories` (10% weight)
- `/api/postgresql/brands` (10% weight)

## Output Formats & Visualization

K6 provides multiple ways to document and visualize test results:

### 1. Console Output (Built-in)
Each test outputs a detailed summary to the console including:
- Request metrics (RPS, duration, percentiles)
- Error rates
- Virtual user statistics
- Slow request tracking

### 2. JSON Output
Test results are automatically saved to:
- `k6-results/load-test-results.json`
- `k6-results/spike-test-results.json`
- `k6-results/stress-test-results.json`

Additionally, each test generates a summary JSON file:
- `k6-results-load-test.json`
- `k6-results-spike-test.json`
- `k6-results-stress-test.json`

### 3. HTML Reports (Recommended)

Generate HTML reports using the `k6-reporter` tool:

```bash
# Install k6-reporter (requires Node.js)
npm install -g k6-reporter

# Generate HTML report
k6-reporter k6-results/load-test-results.json -o k6-results/load-test-report.html
```

Or use the official K6 HTML output extension:

```bash
# Add to docker-compose entrypoint:
--out json=/results/test-results.json --out html=/results/test-report.html
```

### 4. Grafana Integration (Real-time Dashboards)

For real-time visualization during test execution:

#### Option A: InfluxDB + Grafana

1. Add InfluxDB and Grafana to docker-compose.yml:
```yaml
  influxdb:
    image: influxdb:2.7
    ports:
      - "8086:8086"
    volumes:
      - influxdb_data:/var/lib/influxdb2

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3001:3000"
    volumes:
      - grafana_data:/var/lib/grafana
```

2. Run K6 with InfluxDB output:
```bash
k6 run --out influxdb=http://influxdb:8086/k6 /scripts/product_load_test.js
```

3. Configure Grafana to connect to InfluxDB and import K6 dashboard templates

#### Option B: K6 Cloud (Paid Service)

K6 Cloud provides hosted dashboards with:
- Real-time test monitoring
- Historical test comparisons
- Team collaboration features
- Advanced analytics

```bash
# Login to K6 Cloud
k6 login cloud --token YOUR_TOKEN

# Run test with cloud output
k6 cloud /scripts/product_load_test.js
```

### 5. CSV Export

Export metrics to CSV for analysis in Excel/Google Sheets:

```bash
k6 run --out csv=/results/test-results.csv /scripts/product_load_test.js
```

### 6. Custom Visualization Tools

#### Using Python (matplotlib/pandas)
```python
import json
import pandas as pd
import matplotlib.pyplot as plt

with open('k6-results/load-test-results.json') as f:
    data = json.load(f)

# Extract metrics and create visualizations
# Example: Plot response times over time
```

#### Using JavaScript/Node.js
```javascript
const fs = require('fs');
const data = JSON.parse(fs.readFileSync('k6-results/load-test-results.json'));

// Process and visualize with Chart.js, D3.js, etc.
```

## Metrics Explained

### Key Performance Indicators

- **RPS (Requests Per Second)**: Throughput - how many requests the system handles
- **Response Time (p95/p99)**: Latency percentiles - 95% or 99% of requests complete within this time
- **Error Rate**: Percentage of failed requests
- **Virtual Users**: Number of simulated concurrent users

### Thresholds

Each test defines performance thresholds:
- **Load Test**: <1% failures, p95 < 1s
- **Spike Test**: <5% failures, p95 < 2s (relaxed for spike conditions)
- **Stress Test**: <1% failures, p95 < 1s

## Best Practices

1. **Run tests during off-peak hours** to avoid impacting production
2. **Start with lower VU counts** and gradually increase
3. **Monitor system resources** (CPU, memory, database) during tests
4. **Compare results** across test runs to track performance trends
5. **Document baseline metrics** before making system changes
6. **Use realistic test data** that matches production patterns

## Troubleshooting

### Tests fail immediately
- Check if Spring application is running and healthy
- Verify network connectivity between containers
- Check application logs for errors

### High error rates
- Review application logs
- Check database connection pool settings
- Verify system resources (CPU/memory)

### Slow response times
- Check database query performance
- Review application thread pool configuration
- Monitor system resource usage

## Additional Resources

- [K6 Documentation](https://k6.io/docs/)
- [K6 Results Output](https://k6.io/docs/results-output/)
- [K6 Metrics Reference](https://k6.io/docs/using-k6/metrics/)
- [Grafana K6 Dashboard](https://grafana.com/grafana/dashboards/2587)

