package ru.kniturkai.urlvault.dto;

import java.time.Instant;

public record BookmarkResponse(
        Long id,
        String title,
        String url,
        String description,
        String tag,
        Instant createdAt
) {}
