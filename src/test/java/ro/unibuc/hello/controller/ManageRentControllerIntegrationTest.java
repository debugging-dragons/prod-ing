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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.unibuc.hello.data.Game;
import ro.unibuc.hello.data.Rent;
import ro.unibuc.hello.data.RentRepository;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.dto.LoginRequest;
import ro.unibuc.hello.dto.LoginResponse;
import ro.unibuc.hello.dto.RegisterRequest;
import ro.unibuc.hello.service.AuthenticationService;
import ro.unibuc.hello.service.GameService;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class ManageRentControllerIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));
        registry.add("mongodb.connection.url", () -> "mongodb://localhost:" + PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameService gameService;

    @Autowired
    private RentRepository rentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    private String token;
    private String gameId;

    @BeforeAll
    public static void startContainer() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void stopContainer() {
        mongoDBContainer.stop();
    }

    @BeforeEach
    public void setup() {
        // Clean up DB
        rentRepository.deleteAll();
        gameService.deleteAllGames();
        userRepository.deleteAll();

        // Register user & generate token
        authenticationService.register(new RegisterRequest("tester", "password"));
        LoginResponse loginResponse = authenticationService.login(new LoginRequest("tester", "password"));
        token = loginResponse.getToken();

        // Create game
        Game game = new Game("Portal", "PC", "Puzzle", 2007);
        Game savedGame = gameService.createGame(game);
        gameId = savedGame.getId();

        // Create late rent (3-day rent made 5 days ago)
        Rent rent = new Rent("tester", gameId, 3);
        rent.setRentDate(LocalDateTime.now().minusDays(5));
        rentRepository.save(rent);
    }

    @Test
    public void testGetAllActiveRents() throws Exception {
        mockMvc.perform(get("/manage/rented")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value("tester"))
                .andExpect(jsonPath("$[0].gameId").value(gameId));
    }

    @Test
    public void testGetLateRenters() throws Exception {
        mockMvc.perform(get("/manage/late")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value("tester"))
                .andExpect(jsonPath("$[0].gameId").value(gameId));
    }
}