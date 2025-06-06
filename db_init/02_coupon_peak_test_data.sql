USE hhplus;

-- MySQL 재귀 CTE 최대 깊이 설정 (10만 명을 위해 충분히 큰 값으로 설정)
SET @@cte_max_recursion_depth = 1000000; -- 100만으로 설정하여 10만 명 삽입 충분

-- 외래키 제약 조건 잠시 비활성화 (대량 삽입 시 속도 향상 및 제약 조건 오류 방지)
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터 truncate (테스트마다 깨끗한 상태로 시작)
TRUNCATE TABLE coupon_history;
TRUNCATE TABLE user;
TRUNCATE TABLE coupon;

-- 100,000명의 사용자 데이터 삽입
-- RECURSIVE CTE를 사용하여 대량의 연속된 데이터를 생성합니다.
INSERT INTO user (username, email, password, status, created_at, updated_at)
WITH RECURSIVE user_numbers (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM user_numbers WHERE n < 100000 -- 10만 명 (K6 스크립트 VU 범위에 맞춰)
)
SELECT
    CONCAT('test_user_', n),
    CONCAT('user', n, '@example.com'),
    'hashed_password_123', -- 실제 앱에서 사용하는 비밀번호 정책에 맞춰
    'ACTIVE', -- UserStatus enum
    NOW(),
    NOW()
FROM user_numbers;

-- 선착순 1만 개 한정 쿠폰 데이터 삽입 (ID는 AUTO_INCREMENT로 자동 할당)
INSERT INTO coupon (coupon_name, discount_type, discount_value, max_quantity, issued_quantity, coupon_status, valid_start_date, valid_end_date, coupon_type, issue_status)
VALUES (
           '선착순 50% 할인 쿠폰',
           'FIXED_RATE',
           50.00,
           10000,
           0,
           'ACTIVE',
           CURDATE(),
           DATE_ADD(CURDATE(), INTERVAL 7 DAY),
           'LIMITED',  -- <--- 'PROMOTION' 대신 'LIMITED'로 수정
           'READY'
       );

-- 외래키 제약 조건 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;
