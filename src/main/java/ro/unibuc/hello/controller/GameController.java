package ro.unibuc.hello.controller;

import ro.unibuc.hello.data.Game;
import ro.unibuc.hello.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/slow")
    public ResponseEntity<List<Game>> getAllGamesSlow() throws InterruptedException {
        Thread.sleep(12000);
        List<Game> games = gameService.getAllGames();
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @GetMapping("/create-game-error")
    public ResponseEntity<String> triggerCreateGameError() {
        try {
            // Create a game with invalid data to trigger an error
            Game invalidGame = new Game("", "", "", -1);
            gameService.createGame(invalidGame);
            return new ResponseEntity<>("This should not happen", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error triggered: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/memory")
    public String consumeMemory() {
        List<byte[]> memoryConsumer = new ArrayList<>();
        
        // Create and add 10 arrays of 10MB each
        for (int i = 0; i < 10; i++) {
            memoryConsumer.add(new byte[10 * 1024 * 1024]); // 10MB per array
            try {
                Thread.sleep(500); // Sleep to allow monitoring to catch up
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return "Memory consumption test completed";
    }

    @GetMapping
    @Timed(value = "games.getAll", description = "Time taken to return all games")
    @Counted(value = "games.getAll.count", description = "Number of times getAllGames method has been invoked")
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Timed(value = "games.getById", description = "Time taken to return a game by id")
    @Counted(value = "games.getById.count", description = "Number of times getGameById method has been invoked")
    public ResponseEntity<Game> getGameById(@PathVariable String id) {
        Game game = gameService.getGameById(id);
        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @PostMapping
    @Timed(value = "games.create", description = "Time taken to create a new game")
    @Counted(value = "games.create.count", description = "Number of times createGame method has been invoked")
    public ResponseEntity<Game> addGame(@Valid @RequestBody Game game) {
        Game newGame = gameService.createGame(game);
        return new ResponseEntity<>(newGame, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Timed(value = "games.update", description = "Time taken to update a game")
    @Counted(value = "games.update.count", description = "Number of times updateGame method has been invoked")
    public ResponseEntity<Game> updateGame(@PathVariable String id, @Valid @RequestBody Game game) {
        Game updatedGame = gameService.updateGame(id, game);
        return new ResponseEntity<>(updatedGame, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Timed(value = "games.delete", description = "Time taken to delete a game")
    @Counted(value = "games.delete.count", description = "Number of times deleteGame method has been invoked")
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        gameService.deleteGame(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}