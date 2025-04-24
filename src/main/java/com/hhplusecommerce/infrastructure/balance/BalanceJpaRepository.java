package com.hhplusecommerce.infrastructure.balance;

import com.hhplusecommerce.domain.balance.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BalanceJpaRepository extends JpaRepository<UserBalance, Long> {
    @Query("SELECT ub FROM UserBalance ub WHERE ub.userId = :userId")
    Optional<UserBalance> findByUserId(@Param("userId") Long userId);
}
