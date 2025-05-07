package ro.unibuc.hello.service;

import ro.unibuc.hello.data.Game;
import ro.unibuc.hello.data.GameRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.exception.EntityNotFoundException;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final MeterRegistry metricsRegistry;
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    public GameService(GameRepository gameRepository, MeterRegistry metricsRegistry) {
        this.gameRepository = gameRepository;
        this.metricsRegistry = metricsRegistry;
    }

    public List<Game> getAllGames() {
        metricsRegistry.counter("game_service_calls", "method", "getAllGames").increment();
        
        long startTime = System.nanoTime();
        List<Game> games = gameRepository.findAll();
        long duration = System.nanoTime() - startTime;
        
        metricsRegistry.timer("game_service_time", "method", "getAllGames").record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
        
        return games;
    }

    public Game getGameById(String id) {
        metricsRegistry.counter("game_service_calls", "method", "getGameById").increment();
        
        long startTime = System.nanoTime();
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + id));
        long duration = System.nanoTime() - startTime;
        
        metricsRegistry.timer("game_service_time", "method", "getGameById").record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
        
        return game;
    }

    public Game createGame(Game game) {
        metricsRegistry.counter("game_service_calls", "method", "createGame").increment();
    
        long startTime = System.nanoTime();
        try {
            Game savedGame = gameRepository.save(game);
            long duration = System.nanoTime() - startTime;
            metricsRegistry.timer("game_service_time", "method", "createGame").record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
            return savedGame;
        } catch (Exception e) {
            // Record the error
            metricsRegistry.counter("game_service_errors", "method", "createGame", "exception", e.getClass().getSimpleName()).increment();
            throw e;
        }
    }

    public Game updateGame(String id, Game gameDetails) {
        metricsRegistry.counter("game_service_calls", "method", "updateGame").increment();
        
        long startTime = System.nanoTime();
        
        // We're not recording the internal getGameById call separately since we're
        // tracking the whole updateGame operation
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + id));
                
        game.setName(gameDetails.getName());
        game.setPlatform(gameDetails.getPlatform());
        game.setGenre(gameDetails.getGenre());
        game.setReleasedYear(gameDetails.getReleasedYear());

        Game updatedGame = gameRepository.save(game);
        long duration = System.nanoTime() - startTime;
        
        metricsRegistry.timer("game_service_time", "method", "updateGame").record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
        
        return updatedGame;
    }

    public void deleteGame(String id) {
        metricsRegistry.counter("game_service_calls", "method", "deleteGame").increment();
        
        long startTime = System.nanoTime();
        
        // We're not recording the internal getGameById call separately since we're
        // tracking the whole deleteGame operation
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + id));
                
        gameRepository.delete(game);
        long duration = System.nanoTime() - startTime;
        
        metricsRegistry.timer("game_service_time", "method", "deleteGame").record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    public void deleteAllGames() {
        metricsRegistry.counter("game_service_calls", "method", "deleteAllGames").increment();
        
        long startTime = System.nanoTime();
        gameRepository.deleteAll();
        long duration = System.nanoTime() - startTime;
        
        metricsRegistry.timer("game_service_time", "method", "deleteAllGames").record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
    }
}