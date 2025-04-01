#  🔍️ E-commerce 서비스 시퀀스 다이어그램

## 1. 기능 전체

<details>
<summary> 펼치기 </summary>

```mermaid
sequenceDiagram
    participant 사용자
    participant 상품
    participant 재고
    participant 잔액
    participant 쿠폰
    participant 결제
    participant 외부시스템

    %% 상품 조회
    사용자->>상품: 상품 목록 조회 요청
    상품-->>사용자: 상품 정보 응답

    %% 인기 상품 조회
    사용자->>상품: 인기 상품 조회 요청
    상품-->>사용자: 인기 상품 응답

    %% 잔액 충전
    사용자->>잔액: 잔액 충전 요청
    잔액-->>사용자: 잔액 충전 완료

    %% 주문 및 결제
    사용자->>결제: 주문 요청 (상품 ID, 수량, 쿠폰)

    %% 재고 확인
    결제->>상품: 상품 재고 조회
    상품->>재고: 재고 수량 조회
    alt 재고 부족
        결제-->>사용자: 재고 부족 (다른 상품을 선택해 주세요)
    else 재고 충분
        결제->>쿠폰: 쿠폰 사용 여부 확인
        alt 쿠폰 사용 불가
            결제-->>사용자: 쿠폰 사용 불가
        else 쿠폰 할인 적용
            쿠폰-->>결제: 쿠폰 할인 금액 적용
        end

        %% 결제 진행
        결제->>잔액: 잔액 차감 요청
        alt 잔액 부족
            결제-->>사용자: 잔액 부족
        else 잔액 충분
            %% 결제 완료 후 마지막 처리 (잔액 차감, 쿠폰 적용, 재고 차감)
            잔액->>잔액: 잔액 차감
            결제->>상품: 상품 재고 차감
            상품-->>재고: 재고 수량 차감
            결제->>외부시스템: 주문 정보 전송
            외부시스템-->>결제: 전송 완료
            결제-->>사용자: 결제 완료
        end
    end
```

</details>

<br>

---

<br>

## 2. 기능별 상세

### ✅ 상품 조회
<details>
<summary> 펼치기 </summary>

> ※ 초기 시퀀스 다이어그램은 RDB 기반으로 작성합니다.  
> 추후 성능 최적화 시 별도 **캐시 저장소(ex. Redis)** 를 통해 관리하는 구조로 확장합니다.
<br>

```mermaid
sequenceDiagram
    participant 사용자
    participant 상품
    participant 상품DB
    participant 재고
    participant 재고DB

    사용자->>상품: 상품 목록 조회 요청
    activate 상품

    상품->>상품DB: 상품 정보 목록 조회
    alt [상품 정보 목록 조회 성공]
        상품DB-->>상품: 상품 리스트 반환
        상품->>재고: 상품별 재고 수량 조회
        재고->>재고DB: 재고 수량 조회
        alt [재고 수량 조회 성공]
            재고DB-->>재고: 재고 수량 반환
        else [재고 수량 조회 실패]
            재고DB-->>재고: 오류 발생
        end
        재고-->>상품: 재고 조회 결과
        상품-->>사용자: 응답 (성공 or 일부 재고 조회 실패 포함)
    else [상품 정보 목록 조회 실패]
        상품DB-->>상품: 오류 발생
        상품-->>사용자: 상품 정보 조회 실패 응답
    end
    deactivate 상품
```

</details>

<br>

---


### ✅ 인기 상품 조회
<details>
<summary> 펼치기 </summary>

> ※ 초기 시퀀스 다이어그램은 RDB 기반으로 작성합니다. <br>
> 추후 성능 최적화 시 별도 **캐시 저장소(ex. Redis)** 를 통해 관리하는 구조로 확장합니다.

```mermaid
sequenceDiagram
    participant 사용자
    participant 인기상품조회
    participant 통계DB
    participant 상품
    participant 상품DB

    사용자->>인기상품조회: 인기 상품 목록 요청
    activate 인기상품조회

    인기상품조회->>통계DB: 최근 3일간 판매량 기준 상위 5개 상품 ID 조회
    activate 통계DB
    통계DB-->>인기상품조회: 인기 상품 ID 리스트 반환
    deactivate 통계DB

    인기상품조회->>상품: 상품 상세 정보 요청
    activate 상품
    상품->>상품DB: 상품 정보 조회
    activate 상품DB
    상품DB-->>상품: 상품 상세 정보 반환
    deactivate 상품DB
    상품-->>인기상품조회: 상품 정보 반환
    deactivate 상품

    인기상품조회-->>사용자: 인기 상품 목록 응답
    deactivate 인기상품조회

```
</details>

<br>

---

### ✅ 잔액 충전
<details>
<summary> 펼치기 </summary>

```mermaid
sequenceDiagram
    participant 사용자
    participant 잔액
    participant 잔액DB

    사용자->>잔액: 잔액 충전 요청 (충전 금액 포함)
    activate 잔액

    alt 사용자 존재 & 금액 유효
        잔액->>잔액DB: 사용자 잔액 증가 처리
        activate 잔액DB
        잔액DB-->>잔액: 처리 완료
        deactivate 잔액DB
        잔액-->>사용자: 충전 완료 응답
    else 사용자 없음 또는 금액 오류
        잔액-->>사용자: 충전 실패 응답 (사유 포함)
    end

    deactivate 잔액

```

</details>

<br>

---


### ✅ 잔액 조회
<details>
<summary> 펼치기 </summary>

```mermaid
sequenceDiagram
    participant 사용자
    participant 잔액
    participant 잔액DB

    사용자->>잔액: 잔액 조회 요청
    activate 잔액

    alt 사용자 존재 & 조회 성공
        잔액->>잔액DB: 사용자 잔액 조회
        activate 잔액DB
        잔액DB-->>잔액: 잔액 반환
        deactivate 잔액DB
        잔액-->>사용자: 잔액 응답
    else 사용자 없음 또는 DB 오류
        잔액-->>사용자: 잔액 조회 실패 응답 (에러 메시지 포함)
    end

    deactivate 잔액

```

</details>

<br>

---


### ✅ 쿠폰 발급
<details>
<summary> 펼치기 </summary>

> ※ 현재는 RDB 기반으로 작성됩니다.  
> 추후 성능 최적화 시, 쿠폰 발급 및 조회는 **Redis**를 사용하여 실시간 처리합니다.  
> RDB는 전체 쿠폰 수량 관리 및 일관성 유지에 사용되며, <br>
> Redis는 **실시간 발급**과 **조회**를 빠르게 처리합니다.

```mermaid
sequenceDiagram
    participant 사용자
    participant 쿠폰
    participant 쿠폰DB

    사용자->>쿠폰: 쿠폰 발급 요청
    activate 쿠폰
    쿠폰->>쿠폰DB: 쿠폰 발급 가능 여부 확인
    activate 쿠폰DB
    alt 쿠폰 발급 가능
        쿠폰->>쿠폰DB: 쿠폰 발급 처리 (잔여 수량 차감)
        쿠폰DB-->>쿠폰: 발급 완료
        쿠폰-->>사용자: 발급된 쿠폰 응답
    else 쿠폰 발급 불가
        쿠폰-->>사용자: 발급 불가 응답
    end
    deactivate 쿠폰DB
    deactivate 쿠폰

```


</details>

<br>

---

### ✅ 쿠폰 조회
<details>
<summary> 펼치기 </summary>

```mermaid
sequenceDiagram
    participant 사용자
    participant 쿠폰
    participant 쿠폰DB

    사용자->>쿠폰: 보유 쿠폰 목록 조회 요청
    activate 쿠폰
    alt 사용자 존재 & 조회 성공
        쿠폰->>쿠폰DB: 사용자 보유 쿠폰 조회
        activate 쿠폰DB
        쿠폰DB-->>쿠폰: 보유 쿠폰 목록 반환 (0개 가능)
        deactivate 쿠폰DB
        쿠폰-->>사용자: 쿠폰 목록 응답
    else 사용자 없음 or DB 오류
        쿠폰-->>사용자: 쿠폰 조회 실패 응답
    end
    deactivate 쿠폰

```

</details>

<br>

---

### ✅ 쿠폰 사용
<details>
<summary> 펼치기 </summary>

```mermaid
sequenceDiagram
    participant 사용자
    participant 쿠폰
    participant 쿠폰DB

    사용자->>쿠폰: 쿠폰 사용 요청
    activate 쿠폰
    쿠폰->>쿠폰DB: 쿠폰 상태 확인 (유효성 검사)
    activate 쿠폰DB

    alt 쿠폰 사용 가능
        쿠폰->>쿠폰DB: 쿠폰 사용 처리 (상태 변경)
        쿠폰DB-->>쿠폰: 사용 처리 완료
        쿠폰-->>사용자: 쿠폰 사용 완료 응답
    else 쿠폰 사용 불가
        쿠폰-->>사용자: 쿠폰 사용 불가 응답
    end
    deactivate 쿠폰DB
    deactivate 쿠폰
```

</details>

<br>

---

### ✅ 주문
<details>
<summary> 펼치기 </summary>

```mermaid
sequenceDiagram
    participant 사용자
    participant 주문
    participant 쿠폰
    participant 잔액

    사용자->>주문: 주문 요청 (상품, 수량, 쿠폰)
    activate 주문

    주문->>쿠폰: 쿠폰 유효성 확인
    activate 쿠폰
    쿠폰-->>주문: 할인 금액 or 사용 불가
    deactivate 쿠폰

    주문->>잔액: 결제 가능 여부 확인 (최종 결제 금액)
    activate 잔액
    잔액-->>주문: 결제 가능 or 잔액 부족
    deactivate 잔액

    주문-->>사용자: 주문 결과 응답
    deactivate 주문
```

</details>

<br>

---

### ✅ 결제
<details>
<summary> 펼치기 </summary>

```mermaid
sequenceDiagram
    participant 주문
    participant 결제
    participant 잔액
    participant 쿠폰
    participant 외부시스템

    주문->>결제: 결제 요청 (주문 ID, 금액, 쿠폰 정보)
    activate 결제

    alt 결제 조건 만족
        결제->>잔액: 잔액 차감 요청
        activate 잔액
        alt 잔액 차감 성공
            잔액-->>결제: 차감 완료
            결제->>쿠폰: 쿠폰 사용 처리
            activate 쿠폰
            쿠폰-->>결제: 사용 완료
            deactivate 쿠폰

            결제->>외부시스템: 주문 정보 전송
            activate 외부시스템
            외부시스템-->>결제: 전송 완료
            deactivate 외부시스템

            결제-->>주문: 결제 성공
        else 잔액 부족 또는 실패
            잔액-->>결제: 차감 실패
            결제-->>주문: 결제 실패 (잔액 부족)
        end
        deactivate 잔액
    else 결제 조건 불만족
        결제-->>주문: 결제 실패 (요청 오류)
    end

    deactivate 결제
```

</details>

<br>
