import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 10 },  // ramp up to 10 users
        { duration: '30s', target: 50 },  // sustain load
        { duration: '10s', target: 0 },   // ramp down
    ],
    thresholds: {
        http_req_duration: ['p(90)<500'], // 90% requests < 500ms
    },
};

export default function () {
    let res = http.get('http://spring-app:8080/api/postgresql/products');
    check(res, {
        'status was 200': r => r.status === 200,
    });
    sleep(1);
}
