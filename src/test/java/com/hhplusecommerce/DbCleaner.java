package com.hhplusecommerce;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.metamodel.Type.PersistenceType.ENTITY;

@Slf4j
@Component
@Profile("test")
public class DbCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    private final List<String> tables = new ArrayList<>();

    @PostConstruct
    public void init() {
        tables.addAll(
                entityManager.getMetamodel().getEntities().stream()
                        .filter(e -> e.getPersistenceType().equals(ENTITY))
                        .map(e -> e.getJavaType().getAnnotation(jakarta.persistence.Table.class))
                        .filter(Objects::nonNull)
                        .map(Table::name)
                        .toList()
        );
        log.info("DbCleaner 초기화 완료: 테이블 수 = {}", tables.size());
    }

    @Transactional
    public void execute() {
        log.info("DbCleaner 실행 시작");

        entityManager.flush();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        for (String table : tables) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
            try {
                entityManager.createNativeQuery("ALTER TABLE " + table + " AUTO_INCREMENT = 1").executeUpdate();
            } catch (Exception e) {
                log.warn("AUTO_INCREMENT 초기화 실패: {}", table, e);
            }
        }

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }
}
