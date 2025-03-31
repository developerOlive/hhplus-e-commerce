#  🔍️ E-commerce 서비스 시퀀스 다이어그램

## 1. 기능 전체

<details>
<summary> 펼치기 </summary>

<img src="images/img.png" style="max-width: 100%; width: 80%; height: auto;">

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

![img_1.png](images/img_1.png)

</details>

<br>

---


### ✅ 인기 상품 조회
<details>
<summary> 펼치기 </summary>

> ※ 초기 시퀀스 다이어그램은 RDB 기반으로 작성합니다. <br>
> 추후 성능 최적화 시 별도 **캐시 저장소(ex. Redis)** 를 통해 관리하는 구조로 확장합니다.


![img_2.png](images/img_2.png)

</details>

<br>

---

### ✅ 잔액 충전
<details>
<summary> 펼치기 </summary>

![img_3.png](images/img_3.png)

</details>

<br>

---


### ✅ 잔액 조회
<details>
<summary> 펼치기 </summary>

![img_4.png](images/img_4.png)

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

![img_5.png](images/img_5.png)


</details>

<br>

---

### ✅ 쿠폰 조회
<details>
<summary> 펼치기 </summary>

![img_6.png](images/img_6.png)

</details>

<br>

---

### ✅ 쿠폰 사용
<details>
<summary> 펼치기 </summary>

![img_7.png](images/img_7.png)
</details>

<br>

---

### ✅ 주문 및 결제
<details>
<summary> 펼치기 </summary>

![img_8.png](images/img_8.png)
</details>

<br>

---