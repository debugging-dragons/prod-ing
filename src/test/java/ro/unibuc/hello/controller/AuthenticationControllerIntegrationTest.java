package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.hello.dto.LoginRequest;
import ro.unibuc.hello.dto.RegisterRequest;
import ro.unibuc.hello.service.AuthenticationService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("IntegrationTest")
public class AuthenticationControllerIntegrationTest {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Use the existing MongoDB container running in Docker
        registry.add("spring.data.mongodb.host", () -> "host.docker.internal");
        registry.add("spring.data.mongodb.port", () -> 27017);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationService authenticationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUpBeforeEach() {
        objectMapper = new ObjectMapper();

        authenticationService.deleteAllUsers();

        RegisterRequest registerRequest = new RegisterRequest("testUser", "password123");
        authenticationService.register(registerRequest);
    }

    @Test
    public void testLogin() throws Exception {
        // Prepare data for login
        LoginRequest loginRequest = new LoginRequest("testUser", "password123");

        // Call the endpoint and check response
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testRegister() throws Exception {
        // Prepare data for registration
        RegisterRequest registerRequest = new RegisterRequest("newUser", "password123");

        // Call the endpoint and check response
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogin_InvalidCredentials() throws Exception {
        // Prepare data for invalid login
        LoginRequest invalidLoginRequest = new LoginRequest("wrongUser", "wrongPassword");

        // Call the endpoint and check response
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized());
    }
}