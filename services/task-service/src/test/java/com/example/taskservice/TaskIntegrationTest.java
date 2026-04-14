package com.example.taskservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.example.taskservice.support.JwtTestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integrationtest")
class TaskIntegrationTest {

    private static final String JWT_SECRET = "test-secret-key-that-is-long-enough-for-hmac-signing-12345";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndListTasksWithJwt() throws Exception {
        String token = JwtTestTokens.accessTokenForUser("alice", JWT_SECRET);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Integration title","description":"Integration body","status":"OPEN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration title"))
                .andExpect(jsonPath("$.ownerUsername").value("alice"));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Integration title"));
    }

    @Test
    void listTasksWithoutTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTasksWithMalformedTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer not-a-jwt-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTasksWithExpiredTokenReturnsForbidden() throws Exception {
        String token = JwtTestTokens.expiredTokenForUser("alice", JWT_SECRET);

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotReadAnotherUsersTask() throws Exception {
        String aliceToken = JwtTestTokens.accessTokenForUser("alice", JWT_SECRET);
        Long taskId = createTaskAndGetId(aliceToken);
        String bobToken = JwtTestTokens.accessTokenForUser("bob", JWT_SECRET);

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTaskWithBlankTitleReturnsBadRequest() throws Exception {
        String token = JwtTestTokens.accessTokenForUser("alice", JWT_SECRET);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","description":"Integration body","status":"OPEN"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTaskWithInvalidStatusReturnsBadRequest() throws Exception {
        String token = JwtTestTokens.accessTokenForUser("alice", JWT_SECRET);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Integration title","description":"Integration body","status":"INVALID_STATUS"}
                                """))
                .andExpect(status().isBadRequest());
    }

    private Long createTaskAndGetId(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Owned task","description":"Created for access test","status":"OPEN"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Number id = JsonPath.read(responseJson, "$.id");
        return id.longValue();
    }
}
