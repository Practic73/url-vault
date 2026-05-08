package ru.kniturkai.urlvault.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kniturkai.urlvault.model.Bookmark;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("SELECT b FROM Bookmark b WHERE " +
           "(:q IS NULL OR :q = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:tag IS NULL OR :tag = '' OR b.tag = :tag) " +
           "ORDER BY b.createdAt DESC")
    List<Bookmark> search(@Param("q") String q, @Param("tag") String tag);

    List<Bookmark> findAllByOrderByCreatedAtDesc();

    long count();
}
