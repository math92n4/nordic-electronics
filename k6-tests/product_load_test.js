import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// Track active VUs per request
const vusTrend = new Trend('vus_active');

// Counter for requests >1 second
export let slowRequests = new Counter('slow_requests');
// Track which VUs had slow requests
export let slowVUs = new Trend('slow_vus');

export let options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '10s', target: 50 },
        { duration: '10s', target: 100 },
        { duration: '10s', target: 200 },
        { duration: '10s', target: 300 },
        { duration: '10s', target: 400 },
        { duration: '10s', target: 500 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],    // fail test if >1% requests fail
        http_req_duration: ['p(95)<1000'], // fail if p95 > 1s
    }
};

export default function () {
    let res = http.get('http://spring-app:8080/api/postgresql/products');

    check(res, { 'status was 200': r => r.status === 200 });

    // Track slow requests
    if (res.timings.duration > 1000) { // 1 second threshold
        slowRequests.add(1);
        slowVUs.add(__VU);
    }

    sleep(1);
}

export function handleSummary(data) {
    const metrics = data.metrics;

    // Safe access for slow requests
    const slowCount = metrics.slow_requests?.values?.count || 0;
    const avgVU = metrics.slow_vus?.values?.avg ? metrics.slow_vus.values.avg.toFixed(2) : 0;

    // Safe access for HTTP metrics
    const avg = metrics.http_req_duration?.values?.avg ? metrics.http_req_duration.values.avg / 1000 : 0;
    const max = metrics.http_req_duration?.values?.max ? metrics.http_req_duration.values.max / 1000 : 0;
    const min = metrics.http_req_duration?.values?.min ? metrics.http_req_duration.values.min / 1000 : 0;

    const rps = metrics.http_reqs?.values?.rate || 0;
    const failures = metrics.http_req_failed?.values?.rate ? metrics.http_req_failed.values.rate * 100 : 0;

    console.log("\n================= PERFORMANCE SUMMARY =================");
    console.log(`Requests per second:      ${rps.toFixed(2)} RPS`);
    console.log(`Avg request duration:     ${avg.toFixed(2)} seconds`);
    console.log(`Min request duration:     ${min.toFixed(2)} seconds`);
    console.log(`Max request duration:     ${max.toFixed(2)} seconds`);
    console.log(`Failure rate:             ${failures.toFixed(2)} %`);

    console.log("\n================= SLOW REQUEST SUMMARY =================");
    console.log(`Total slow requests (>1s):      ${slowCount}`);
    console.log(`Avg VU for slow requests:        ${avgVU}`);
    console.log("=======================================================\n");

    return {};
}
