import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { SharedArray } from 'k6/data';

const BASE_URL = __ENV.BASE_URL;
const COUPON_ID = __ENV.COUPON_ID || '1';

const userIds = new SharedArray('user pool', () => {
    const users = [];
    for (let i = 1; i <= 100000; i++) {
        users.push(i);
    }
    return users;
});

export const options = {
    scenarios: {
        safe_coupon_issuance_test: {
            executor: 'ramping-arrival-rate',
            startRate: 0,
            timeUnit: '1s',
            preAllocatedVUs: 1000, // 최소 1000 이상 확보
            maxVUs: 2000,           // 최대 도달 목표
            stages: [
                { target: 300, duration: '20s' },
                { target: 600, duration: '20s' },
                { target: 900, duration: '20s' },
                { target: 1200, duration: '20s' },
                { target: 1500, duration: '20s' },
                { target: 1800, duration: '20s' },
                { target: 2000, duration: '20s' },
                { target: 2000, duration: '1m' },
                { target: 0, duration: '30s' },
            ],
            gracefulStop: '10s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
        checks: ['rate>0.99'],
    },
};


export default function () {
    const totalUsers = userIds.length;

    let userId;
    if (Math.random() < 0.9) {
        const index = (__VU - 1) * 100 + __ITER;
        if (index < totalUsers) {
            userId = userIds[index];
        } else {
            userId = userIds[Math.floor(Math.random() * totalUsers)];
        }
    } else {
        userId = userIds[Math.floor(Math.random() * totalUsers)];
    }

    const url = `${BASE_URL}/api/v2/users/${userId}/coupons/${COUPON_ID}/issue`;
    const res = http.post(url, null, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(res, {
        '예상된 응답 코드 (200, 409)': (r) => r.status === 200 || r.status === 409,
        '쿠폰 발급 성공 (200)': (r) =>
            r.status === 200 &&
            r.json('success') === true &&
            r.json('data') === '발급 요청 성공',
        '쿠폰 이미 발급됨 (409)': (r) =>
            r.status === 409 &&
            r.json('success') === false &&
            r.json('message') === '이미 발급된 쿠폰입니다.',
    });

    if (res.status >= 500) {
        console.error(
            `서버 오류 발생 (상태 코드: ${res.status}, URL: ${url}, 사용자 ID: ${userId}) - 응답: ${res.body}`
        );
        fail(`치명적인 서버 오류 발생: ${res.status}`);
    }

    sleep(Math.random() * 2 + 1); // 1초 ~ 3초 랜덤하게 sleep
}
