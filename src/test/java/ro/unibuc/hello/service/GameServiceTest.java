package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import ro.unibuc.hello.data.Game;
import ro.unibuc.hello.data.GameRepository;
import ro.unibuc.hello.exception.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;
    
    @Mock
    private MeterRegistry metricsRegistry;
    
    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private static final String GAME_ID = "game123";
    
    @BeforeEach
    void setUp() {
        // Create test game
        testGame = new Game("Test Game", "PC", "Action", 2023);
        testGame.setId(GAME_ID);
    }

    @Test
    void testGetAllGames() {
        // Arrange
        List<Game> games = Arrays.asList(
                testGame,
                new Game("Another Game", "PlayStation", "RPG", 2022)
        );
        
        // Setup counter mock
        Counter counterMock = mock(Counter.class);
        when(metricsRegistry.counter("game_service_calls", "method", "getAllGames")).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        
        // Setup timer mock
        Timer timerMock = mock(Timer.class);
        when(metricsRegistry.timer("game_service_time", "method", "getAllGames")).thenReturn(timerMock);
        
        // Setup repository mock
        when(gameRepository.findAll()).thenReturn(games);

        // Act
        List<Game> result = gameService.getAllGames();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Test Game", result.get(0).getName());
        assertEquals("Another Game", result.get(1).getName());

        // Verify repository mock was called
        verify(gameRepository, times(1)).findAll();
        
        // Verify metrics were recorded
        verify(counterMock, times(1)).increment();
        verify(timerMock, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @Test
    void testGetGameById_ExistingGame() {
        // Arrange - setup the mocks
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(testGame));
        
        // Setup counter mock
        Counter counterMock = mock(Counter.class);
        when(metricsRegistry.counter("game_service_calls", "method", "getGameById")).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        
        // Setup timer mock
        Timer timerMock = mock(Timer.class);
        when(metricsRegistry.timer("game_service_time", "method", "getGameById")).thenReturn(timerMock);

        // Act
        Game result = gameService.getGameById(GAME_ID);

        // Assert
        assertNotNull(result);
        assertEquals(GAME_ID, result.getId());
        assertEquals("Test Game", result.getName());
        assertEquals("PC", result.getPlatform());
        assertEquals("Action", result.getGenre());
        assertEquals(2023, result.getReleasedYear());

        // Verify the repository mock was called
        verify(gameRepository, times(1)).findById(GAME_ID);
        
        // Verify metrics were recorded
        verify(counterMock, times(1)).increment();
        verify(timerMock, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @Test
    void testGetGameById_NonExistingGame() {
        // Arrange
        String nonExistingId = "nonExistingId";
        
        // Setup repository mock
        when(gameRepository.findById(nonExistingId)).thenReturn(Optional.empty());
        
        // Setup counter mock
        Counter counterMock = mock(Counter.class);
        when(metricsRegistry.counter("game_service_calls", "method", "getGameById")).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        
        // Note: We're NOT setting up the timer mock since it won't be used due to exception

        // Act & Assert
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            gameService.getGameById(nonExistingId);
        });

        // Verify exception message if needed
        assertTrue(exception.getMessage().contains(nonExistingId));

        // Verify the repository mock was called
        verify(gameRepository, times(1)).findById(nonExistingId);

        // Verify counter metric was recorded (timer won't complete due to exception)
        verify(counterMock, times(1)).increment();
}

    @Test
    void testCreateGame() {
        // Arrange
        Game newGame = new Game("New Game", "Xbox", "Strategy", 2024);
        Game savedGame = new Game("New Game", "Xbox", "Strategy", 2024);
        savedGame.setId("newGameId");
        
        // Setup repository mock
        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);
        
        // Setup counter mock
        Counter counterMock = mock(Counter.class);
        when(metricsRegistry.counter("game_service_calls", "method", "createGame")).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        
        // Setup timer mock
        Timer timerMock = mock(Timer.class);
        when(metricsRegistry.timer("game_service_time", "method", "createGame")).thenReturn(timerMock);

        // Act
        Game result = gameService.createGame(newGame);

        // Assert
        assertNotNull(result);
        assertEquals("newGameId", result.getId());
        assertEquals("New Game", result.getName());
        assertEquals("Xbox", result.getPlatform());
        assertEquals("Strategy", result.getGenre());
        assertEquals(2024, result.getReleasedYear());

        // Verify the repository mock was called
        verify(gameRepository, times(1)).save(any(Game.class));
        
        // Verify metrics were recorded
        verify(counterMock, times(1)).increment();
        verify(timerMock, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @Test
    void testUpdateGame_ExistingGame() {
        // Arrange
        Game updatedGame = new Game("Updated Game", "Switch", "Adventure", 2021);
        
        // Setup repository mocks
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game savedGame = invocation.getArgument(0);
            return savedGame; // Return the game being saved
        });
        
        // Setup counter mock
        Counter counterMock = mock(Counter.class);
        when(metricsRegistry.counter("game_service_calls", "method", "updateGame")).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        
        // Setup timer mock
        Timer timerMock = mock(Timer.class);
        when(metricsRegistry.timer("game_service_time", "method", "updateGame")).thenReturn(timerMock);

        // Act
        Game result = gameService.updateGame(GAME_ID, updatedGame);

        // Assert
        assertNotNull(result);
        assertEquals(GAME_ID, result.getId());
        assertEquals("Updated Game", result.getName());
        assertEquals("Switch", result.getPlatform());
        assertEquals("Adventure", result.getGenre());
        assertEquals(2021, result.getReleasedYear());

        // Verify the repository mocks were called
        verify(gameRepository, times(1)).findById(GAME_ID);
        verify(gameRepository, times(1)).save(any(Game.class));
        
        // Verify metrics were recorded
        verify(counterMock, times(1)).increment();
        verify(timerMock, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @@Test
void testUpdateGame_NonExistingGame() {
    // Arrange
    String nonExistingId = "nonExistingId";
    Game updatedGame = new Game("Updated Game", "Switch", "Adventure", 2021);
    
    // Setup repository mock
    when(gameRepository.findById(nonExistingId)).thenReturn(Optional.empty());
    
    // Setup counter mock
    Counter counterMock = mock(Counter.class);
    when(metricsRegistry.counter("game_service_calls", "method", "updateGame")).thenReturn(counterMock);
    doNothing().when(counterMock).increment();
    
    // Note: We're NOT setting up the timer mock since it won't be used due to exception

    // Act & Assert
    Exception exception = assertThrows(EntityNotFoundException.class, () -> {
        gameService.updateGame(nonExistingId, updatedGame);
    });

    // Verify exception message if needed
    assertTrue(exception.getMessage().contains(nonExistingId));

    // Verify the repository mocks were called
    verify(gameRepository, times(1)).findById(nonExistingId);
    verify(gameRepository, never()).save(any(Game.class));
    
    // Verify counter metric was recorded
    verify(counterMock, times(1)).increment();
}


    @Test
    void testDeleteGame_ExistingGame() {
        // Arrange
        // Setup repository mocks
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(testGame));
        doNothing().when(gameRepository).delete(any(Game.class));
        
        // Setup counter mock
        Counter counterMock = mock(Counter.class);
        when(metricsRegistry.counter("game_service_calls", "method", "deleteGame")).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        
        // Setup timer mock
        Timer timerMock = mock(Timer.class);
        when(metricsRegistry.timer("game_service_time", "method", "deleteGame")).thenReturn(timerMock);

        // Act
        gameService.deleteGame(GAME_ID);

        // Assert
        // Verify the repository mocks were called
        verify(gameRepository, times(1)).findById(GAME_ID);
        verify(gameRepository, times(1)).delete(testGame);
        
        // Verify metrics were recorded
        verify(counterMock, times(1)).increment();
        verify(timerMock, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @Test
void testDeleteGame_NonExistingGame() {
    // Arrange
    String nonExistingId = "nonExistingId";
    
    // Setup repository mock
    when(gameRepository.findById(nonExistingId)).thenReturn(Optional.empty());
    
    // Setup counter mock
    Counter counterMock = mock(Counter.class);
    when(metricsRegistry.counter("game_service_calls", "method", "deleteGame")).thenReturn(counterMock);
    doNothing().when(counterMock).increment();
    
    // Note: We're NOT setting up the timer mock since it won't be used due to exception

    // Act & Assert
    Exception exception = assertThrows(EntityNotFoundException.class, () -> {
        gameService.deleteGame(nonExistingId);
    });

    // Verify exception message if needed
    assertTrue(exception.getMessage().contains(nonExistingId));

    // Verify the repository mocks were called
    verify(gameRepository, times(1)).findById(nonExistingId);
    verify(gameRepository, never()).delete(any(Game.class));
    
    // Verify counter metric was recorded
    verify(counterMock, times(1)).increment();
}
    
    @Test
    void testDeleteAllGames() {
        // Arrange
        doNothing().when(gameRepository).deleteAll();
        
        // Setup counter mock
        Counter counterMock = mock(Counter.class);
        when(metricsRegistry.counter("game_service_calls", "method", "deleteAllGames")).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        
        // Setup timer mock
        Timer timerMock = mock(Timer.class);
        when(metricsRegistry.timer("game_service_time", "method", "deleteAllGames")).thenReturn(timerMock);

        // Act
        gameService.deleteAllGames();

        // Assert
        // Verify the repository mock was called
        verify(gameRepository, times(1)).deleteAll();
        
        // Verify metrics were recorded
        verify(counterMock, times(1)).increment();
        verify(timerMock, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }
}