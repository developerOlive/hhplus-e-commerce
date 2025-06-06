import http from 'k6/http';
import { check, sleep } from 'k6';

const headers = {
    'User-Agent': 'curl/7.64.1',
    'Accept': '*/*',
    'Referer': 'http://localhost:8080/',
};

// 부하 테스트 설정
export const options = {
    scenarios: {
        popularity_test: {
            executor: 'ramping-arrival-rate',  // 초당 요청 수를 점진적으로 증가시키는 오픈 모델
            startRate: 50,                     // 초기 초당 요청 수 (RPS)
            timeUnit: '10s',                   // rate 기준 단위 (10초마다 요청 수 계산)
            preAllocatedVUs: 50,               // 초기로 확보할 VU 수
            maxVUs: 500,                       // 최대 가상 유저 수
            stages: [
                { target: 200, duration: '30s' },   // 30초 동안 초당 요청을 200까지 증가 (Warming up)
                { target: 5000, duration: '2m' },   // 2분 동안 초당 요청을 5000까지 증가 (부하 증가)
                { target: 5000, duration: '1m' },   // 1분 동안 초당 5000 유지 (Peak 부하)
                { target: 100, duration: '30s' },   // 30초 동안 부하 감소 (Cool down)
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<800'],  // 95% 요청이 800ms 미만이어야 함
        http_req_failed: ['rate<0.01'],    // 실패율 1% 이하 유지
    },
};

// 테스트 대상 API 호출
export default function () {
    const res = http.get(
        'http://localhost:8080/api/v1/products/popular?limit=5&minSold=100&days=7&category=electronics',
        { headers }
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
}
