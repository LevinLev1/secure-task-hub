package com.example.taskservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:taskdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "security.jwt.secret=test-secret-key-that-is-long-enough-for-hmac-signing-12345"
})
class TaskServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
