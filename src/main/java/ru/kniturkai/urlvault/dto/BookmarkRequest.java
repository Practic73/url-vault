package ru.kniturkai.urlvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookmarkRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) String url,
        @Size(max = 500) String description,
        @Size(max = 50) String tag
) {}
