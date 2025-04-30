package com.hhplusecommerce.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestSupport extends TestcontainersConfiguration {

    @Autowired
    private DbCleaner dbCleaner;

    @BeforeEach
    void setUp() {
        dbCleaner.execute();
    }
}
