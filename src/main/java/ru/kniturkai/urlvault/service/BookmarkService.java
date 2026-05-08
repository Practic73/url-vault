package ru.kniturkai.urlvault.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.kniturkai.urlvault.dto.BookmarkRequest;
import ru.kniturkai.urlvault.dto.BookmarkResponse;
import ru.kniturkai.urlvault.model.Bookmark;
import ru.kniturkai.urlvault.repository.BookmarkRepository;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookmarkService {

    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);

    private final BookmarkRepository repository;

    public BookmarkService(BookmarkRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initDemoData() {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new Bookmark(
                "Статья на Хабре",
                "https://habr.com/ru/articles/750000/",
                "Интересная статья на Хабре",
                "articles",
                Instant.now()
        ));
        repository.save(new Bookmark(
                "Spring Boot на GitHub",
                "https://github.com/spring-projects/spring-boot",
                "Репозиторий Spring Boot",
                "dev",
                Instant.now()
        ));
        repository.save(new Bookmark(
                "КНИТУ-КАИ",
                "https://kai.ru/",
                "Официальный сайт КНИТУ-КАИ",
                "university",
                Instant.now()
        ));
        log.info("Загружены демо-данные: 3 закладки");
    }

    public List<BookmarkResponse> findAll(String q, String tag) {
        List<Bookmark> bookmarks;
        boolean hasQuery = q != null && !q.isBlank();
        boolean hasTag = tag != null && !tag.isBlank();
        if (hasQuery || hasTag) {
            bookmarks = repository.search(q, tag);
        } else {
            bookmarks = repository.findAllByOrderByCreatedAtDesc();
        }
        return bookmarks.stream().map(this::toResponse).toList();
    }

    public BookmarkResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Закладка не найдена: " + id));
    }

    public String getUrlById(Long id) {
        return repository.findById(id)
                .map(Bookmark::getUrl)
                .orElseThrow(() -> new NoSuchElementException("Закладка не найдена: " + id));
    }

    public BookmarkResponse create(BookmarkRequest request) {
        Bookmark bookmark = new Bookmark(
                request.title(),
                request.url(),
                request.description(),
                request.tag(),
                Instant.now()
        );
        Bookmark saved = repository.save(bookmark);
        log.info("Сохранена закладка id={}, url={}", saved.getId(), saved.getUrl());
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Закладка не найдена: " + id);
        }
        repository.deleteById(id);
        log.info("Удалена закладка id={}", id);
    }

    private BookmarkResponse toResponse(Bookmark b) {
        return new BookmarkResponse(b.getId(), b.getTitle(), b.getUrl(),
                b.getDescription(), b.getTag(), b.getCreatedAt());
    }
}
