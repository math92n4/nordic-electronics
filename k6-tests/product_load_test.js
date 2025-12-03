import http from 'k6/http';
import { sleep, check } from 'k6';
import { Counter, Gauge, Trend } from 'k6/metrics';


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

    sleep(1);
}
