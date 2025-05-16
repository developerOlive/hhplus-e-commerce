# 📚 Redis 기반 실시간 판매 상품 랭킹 도입 보고서

---

# ✅ 1. Redis를 활용한 이유

이커머스에서 인기 상품 데이터는 주로 메인 화면에 노출되며<br>
사용자 접점이 많아 조회 빈도가 매우 높습니다.<br>
또한 빠르게 판매 수량 변화에 따라 실시간으로 집계되고 반영되어야 하는 특성이 있습니다.<br><br>

이러한 데이터를 매번 RDB에서 처리하게 되면<br>
트래픽 증가에 따른 부하, 응답 지연, 데이터 일관성 저하 등 다양한 문제가 발생할 수 있습니다.<br><br>

실시간 랭킹은 단순 조회뿐만 아니라<br>
판매 데이터를 누적하고 정렬하며 즉시 갱신되어야 하기 때문에<br>
단순 캐시만으로는 한계가 있습니다.<br><br>

이러한 요구사항을 만족하기 위해<br>
짧고 단순한 랭킹 데이터를 빠르게 처리하고 정렬할 수 있는 Redis를 활용하는 것이 효율적이라고 판단하였습니다.<br><br>

<br><br>

---

# ✅ 2. 설계 내용
<br>

> 실시간 상품 랭킹 처리를 위한 Redis 설계는 다음 두 가지 로직으로 구성됩니다.<br><br>
> (1) 랭킹 상품 집계 (일자별) <br>
> (2) 랭킹 조회 및 갱신

<br>


## (1) “랭킹 상품 집계(일자별)” 설계<br>

### 🔵 Redis
- 최종 상품 결제 트랜잭션 커밋 이후<br>
  Redis에 상품별 판매 수량을 `일자 기준`으로 누적 집계<br><br>
- 이유 : 일별 데이터를 조합하여 주간/월간/기간별 랭킹 등 다양한 집계 시 사용 가능하기 때문<br><br>


### 사용한 Redis 자료구조

| 항목    | 설명                                            |
| ----- |-----------------------------------------------|
| 자료구조  | ZSet (Sorted Set)                             |
| Key   | `ranking:product:sales:{category}-{yyyyMMdd}` |
| Value | 상품 ID (`productId`)                           |
| Score | 누적 판매 수량                                      |

<br>

(ex) 명령어 예시
> ZINCRBY {랭킹}:{상품}:{판매량}:{카테고리}-{일자} {누적할 판매 수량} {상품 ID} <br>
> ZINCRBY ranking:product:sales:electronics-20250513 2 10000001

<br>

명령어 `ZINCRBY`를 선택한 이유<br>
- `ZINCRBY`는 Sorted Set의 특정 멤버의 점수(score)를 증가시키는 명령어입니다.<br>
  점수가 변경되면 Redis가 자동으로 정렬 순서를 다시 반영한다는 특장점이 있습니다.<br>
- 이러한 특성 때문에 `랭킹`에 적합하다고 판단하였습니다. <br><br>

<br>

(예시 코드)
```java
  @Override
  public void incrementProductSales(String category, Long productId, String saleDate, int quantity) {
    // 카테고리 + 일자 기준으로 키 생성
    String dailyKey = redisRankingKeyFactory.dailyKey(category, saleDate);
    // ZINCRBY 실행
    rankingZSetAdapter.incrementScore(dailyKey, productId, quantity);
    // TTL 설정
    rankingZSetAdapter.setExpire(dailyKey, Duration.ofDays(30));
  }
```



최초 생성되는 Redis 키 TTL 설정 <br>
- 30일
- 이유 : 이후 생성될 주간·월간·기간별 랭킹 집계에 활용할 수 있도록 일별 데이터의 보존 기간을 충분히 확보하기 위해


<br><br>


### 🔴 RDB
-  RDB는 정합성 보장과 Redis 복구를 위한 보조 저장소로 활용

<br><br><br>

---


## (2) “랭킹 상품 집계” 설계
<br>

### 🟡 유연한 기간 집계를 위한 설계<br>
- ‘3일’, ‘7일’, ‘10일’ 등 조회 기준 N일이 달라지더라도<br>
날짜별 ZSet을 조합하여 유연하게 대응하기 위해<br>
Redis의 `ZUNIONSTORE` 을 사용하였습니다.<br><br>

(예시)
```java
ZUNIONSTORE ranking:product:sales:electronics:3days 3 \
ranking:product:sales:electronics-20250510 \
ranking:product:sales:electronics-20250511 \
ranking:product:sales:electronics-20250512
```

- 첫 번째 인자 ranking:product:sales:electronics:3days는<br>
합산 결과를 저장할 대상 ZSet의 Key입니다.<br><br>

- 두 번째 인자 `3`은<br>
합산할 원본 ZSet의 개수입니다.<br><br>

- 그 뒤에 오는 3개의 키(`electronics-20250510`, `20250511`, `20250512`)는<br>
실제로 합산할 일자별 ZSet들입니다.<br><br>

- 위 명령어는<br>
"전자제품 카테고리에 대해 최근 3일간(10~12일) 판매 데이터를 합산하여<br>
결과를 `ranking:product:sales:electronics:3days` 키에 저장"하는 작업입니다.<br><br>

- 이처럼 `ZUNIONSTORE`는 조회 기간의 유연성을 보장하면서도 <br>
실시간성과 성능을 모두 확보할 수 있어 실시간 랭킹 집계에 적절하다고 판단하였습니다. <br><br>

- 단, Redis에 대한 직접적인 의존도를 줄이기 위해<br>
  내부 구현은 인터페이스 기반으로 추상화하였습니다.<br>
  → 이를 통해 향후 저장소 변경(예: Redis 구조 변경, DB 전환 등) 시에도<br>
  비즈니스 로직을 수정하지 않고 유연하게 대응할 수 있도록 설계하였습니다.<br><br><br>

```java

    /**
    * 인기 상품 랭킹 집계를 위한 추상 인터페이스
     */
    public interface PopularProductRankingAggregator {

      /** 주어진 카테고리와 기간 리스트 기준으로 기간별 인기상품 랭킹 집계 */
      void aggregateRanking(String category, List<Integer> targetDays);

      /** 주어진 카테고리와 기간(일수) 기준으로 인기상품 랭킹 상위 N개 상품ID 조회 */
      List<Long> getTopProductIds(String category, int days, int limit);

      /** 인기상품 상세정보를 캐시에 저장 */
      void saveProductsToCache(List<ProductDataResult> productData);

      /** 상품 판매량 누적 집계 */
      void incrementProductSales(String category, Long productId, String saleDate, int quantity);

      /** 캐시된 인기상품 상세정보 조회 */
      List<PopularProduct> getProductsFromCache(PopularProductSearchCommand command);
}
```

<br><br><br>


### 🟡 상품 랭킹 및 상세정보 스케쥴링 <br><br>

> 🕒 캐시 갱신 스케줄러 로직 <br>

스케줄러는 `매일 00:15에 동작`하며 <br>
`N일간 랭킹에 포함된 상품 ID 목록`을 기준으로<br>
해당 상품들의 상세 정보를 조회 후 캐시에 저장합니다.<br><br>

`당일은 집계 대상에 포함되지 않기 때문`에<br>
전일 기준 랭킹이 안정적으로 집계된 이후인 00:15 시점을 선택하였습니다.<br><br>

인기 상품 캐싱은 하루 1회 갱신으로 충분하다고 판단하였으나<br>
만약 실시간성을 더욱 보장할 필요가 있는 경우<br>
캐시 갱신 주기를 짧게 조정하거나<br>
판매량 급증 상품만을 대상으로 실시간 갱신 로직을 별도로 구성하는 방안도 고려할 수 있습니다.<br><br>

```java
    @Override
    public List<PopularProduct> getProductsFromCache(PopularProductSearchCommand command) {
        if (command == null) {
            log.warn("Search command is null — returning empty list");
            return Collections.emptyList();
        }

        String category = command.category();
        int days = command.days();
        int limit = command.limit();

        String rankingKey = redisRankingKeyFactory.periodKey(category, days);
        Set<String> productIds = rankingZSetAdapter.getTopIds(rankingKey, limit);

        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        return productCacheAdapter.load(new ArrayList<>(productIds));
    }
```
<br><br>

---
<br>

## ✅ 3. 캐싱 성능 보고서

[ 👉 Redis 캐싱 성능 보고서 ](https://github.com/developerOlive/hhplus-e-commerce/blob/main/docs/08_Cashing_Report.md)

<br>

---


## ✅ 4. 결론

Redis의 Sorted Set을 활용해<br>
실시간 판매 집계와 상품 랭킹을 간단하고 고성능으로 처리할 수 있었습니다.<br><br>

단순 조회 캐시를 넘어<br>
데이터 집계, 순위 계산, 선착순 처리 등 다양한 비즈니스 로직도<br>
효율적으로 처리 가능하다는 점을 확인했습니다.<br><br>

다양한 기간 집계 구조와 TTL 설정을 통해<br>
구조적 유연성과 관리 효율성의 장점을 확인할 수 있었습니다.<br><br>

<br>

---
<br><br>

