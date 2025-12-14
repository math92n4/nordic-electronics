import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

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
        { duration: '5s', target: 0 },
        { duration: '30s', target: 50 },   // Normal load
        { duration: '30s', target: 1000 },  // Sudden spike
        { duration: '30s', target: 50 },  // Rapid drop back
        { duration: '5s', target: 0 },

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
    if (res.timings.duration > 2000) { // 2 second threshold
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

    console.log("\n================= SPIKE TEST SUMMARY =================");
    console.log(`Requests per second:      ${rps.toFixed(2)} RPS`);
    console.log(`Avg request duration:     ${avg.toFixed(2)} seconds`);
    console.log(`Min request duration:     ${min.toFixed(2)} seconds`);
    console.log(`Max request duration:     ${max.toFixed(2)} seconds`);
    console.log(`Failure rate:             ${failures.toFixed(2)} %`);

    console.log("\n================= SLOW REQUEST SUMMARY =================");
    console.log(`Total slow requests (>2s):      ${slowCount}`);
    console.log(`Avg VU for slow requests:        ${avgVU}`);
    console.log("=======================================================\n");

    return {};
}