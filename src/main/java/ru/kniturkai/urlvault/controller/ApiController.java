package ru.kniturkai.urlvault.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.kniturkai.urlvault.dto.BookmarkRequest;
import ru.kniturkai.urlvault.dto.BookmarkResponse;
import ru.kniturkai.urlvault.service.BookmarkService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final BookmarkService service;

    public ApiController(BookmarkService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/bookmarks")
    public List<BookmarkResponse> getAll() {
        return service.findAll(null, null);
    }

    @GetMapping("/bookmarks/{id}")
    public BookmarkResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/bookmarks")
    public ResponseEntity<BookmarkResponse> create(@Valid @RequestBody BookmarkRequest request) {
        BookmarkResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/bookmarks/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }
}
