import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 50 },   // 1분 동안 50명 점진적 증가
        { duration: '2m', target: 50 },   // 2분 동안 50명 유지
        { duration: '1m', target: 100 },  // 1분 동안 100명 점진적 증가
        { duration: '1m', target: 100 },  // 1분 동안 100명 유지
        { duration: '1m', target: 0 },    // 1분 동안 점진적 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<800'],
        http_req_failed: ['rate<0.01'],
    },
};

const MAX_USER_ID = 5000;  // 5천명 유저 중 랜덤 선택

export default function () {
    const userId = Math.floor(Math.random() * MAX_USER_ID) + 1;
    const couponId = 1;

    const url = `http://localhost:8080/api/v1/users/${userId}/coupons/${couponId}/issue`;
    const res = http.post(url);

    check(res, {
        'status is 200 or 400 or 409': (r) => [200, 400, 409].includes(r.status),
    });

    sleep(0.1);
}
