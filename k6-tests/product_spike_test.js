import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

// Track active VUs per request
const vusTrend = new Trend('vus_active');

// Counter for requests >1 second
export let slowRequests = new Counter('slow_requests');
// Track which VUs had slow requests
export let slowVUs = new Trend('slow_vus');

// Base URL
const BASE_URL = 'http://spring-app:8080';

// Define endpoints with weighted distribution (simulating realistic user behavior)
const endpoints = [
    { path: '/api/postgresql/products/paginated?page=0&size=24&sortBy=name&sortDirection=asc', weight: 40 }, // Most common - browsing
    { path: '/api/postgresql/products/best-selling', weight: 25 }, // Popular products
    { path: '/api/postgresql/categories', weight: 15 }, // Category browsing
    { path: '/api/postgresql/brands', weight: 12 }, // Brand browsing
    { path: '/api/postgresql/products/best-reviewed', weight: 8 }, // Reviews
];

// Helper function to select endpoint based on weighted distribution
function selectEndpoint() {
    const random = Math.random() * 100;
    let cumulative = 0;
    for (const endpoint of endpoints) {
        cumulative += endpoint.weight;
        if (random <= cumulative) {
            return endpoint.path;
        }
    }
    return endpoints[0].path; // Fallback
}

// Spike Test: Sudden spike in load, then rapid drop
export let options = {
    stages: [
        { duration: '30s', target: 50 },   // Normal load
        { duration: '10s', target: 500 },  // Sudden spike
        { duration: '1m', target: 500 },   // Maintain spike
        { duration: '10s', target: 50 },  // Rapid drop back
        { duration: '30s', target: 50 },  // Normal load recovery
        { duration: '30s', target: 0 },   // Ramp down
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],    // Allow higher failure rate during spike (5%)
        http_req_duration: ['p(95)<2000'], // Allow higher p95 during spike (2s)
    }
};

export default function () {
    const endpoint = selectEndpoint();
    const url = `${BASE_URL}${endpoint}`;
    
    let res = http.get(url);

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
    const p95 = metrics.http_req_duration?.values?.['p(95)'] ? metrics.http_req_duration.values['p(95)'] / 1000 : 0;
    const p99 = metrics.http_req_duration?.values?.['p(99)'] ? metrics.http_req_duration.values['p(99)'] / 1000 : 0;

    const rps = metrics.http_reqs?.values?.rate || 0;
    const totalRequests = metrics.http_reqs?.values?.count || 0;
    const failures = metrics.http_req_failed?.values?.rate ? metrics.http_req_failed.values.rate * 100 : 0;
    const totalFailures = metrics.http_req_failed?.values?.count || 0;

    // VU metrics
    const maxVUs = metrics.vus_max?.values?.value || 0;
    const avgVUs = metrics.vus?.values?.avg || 0;

    console.log("\n================= SPIKE TEST SUMMARY =================");
    console.log(`Test Type:                Spike Test (Sudden Traffic Surge)`);
    console.log(`Total Requests:           ${totalRequests}`);
    console.log(`Requests per second:      ${rps.toFixed(2)} RPS`);
    console.log(`Max Virtual Users:        ${maxVUs}`);
    console.log(`Avg Virtual Users:        ${avgVUs.toFixed(2)}`);
    
    console.log("\n================= RESPONSE TIME METRICS =================");
    console.log(`Min request duration:     ${min.toFixed(3)}s`);
    console.log(`Avg request duration:     ${avg.toFixed(3)}s`);
    console.log(`P95 request duration:    ${p95.toFixed(3)}s`);
    console.log(`P99 request duration:    ${p99.toFixed(3)}s`);
    console.log(`Max request duration:     ${max.toFixed(3)}s`);

    console.log("\n================= ERROR METRICS =================");
    console.log(`Total failures:           ${totalFailures}`);
    console.log(`Failure rate:             ${failures.toFixed(2)}%`);

    console.log("\n================= SLOW REQUEST SUMMARY =================");
    console.log(`Total slow requests (>1s):      ${slowCount}`);
    console.log(`Avg VU for slow requests:        ${avgVU}`);
    console.log("=======================================================\n");

    // Return JSON summary for programmatic access
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
        'k6-results-spike-test.json': JSON.stringify(data, null, 2),
    };
}

