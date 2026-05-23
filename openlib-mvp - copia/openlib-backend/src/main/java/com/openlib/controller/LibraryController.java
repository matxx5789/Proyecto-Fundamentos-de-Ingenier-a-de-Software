package com.openlib.controller;

import com.openlib.domain.Download;
import com.openlib.domain.User;
import com.openlib.dto.response.BookResponse;
import com.openlib.dto.response.DownloadResponse;
import com.openlib.service.BookServicePort;
import com.openlib.service.DownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/library")
@RequiredArgsConstructor
@Tag(name = "Biblioteca Personal")
public class LibraryController {

    private final DownloadService downloadService;
    private final BookServicePort bookService;

    @GetMapping
    @Operation(summary = "Ver biblioteca personal (libros adquiridos)")
    public List<BookResponse> getLibrary(@AuthenticationPrincipal User user) {
        return downloadService.getLibrary(user.getId(), bookService);
    }

    @GetMapping("/{bookId}/download-url")
    @Operation(summary = "Obtener signed URL de descarga (válida 5 min)")
    public DownloadResponse getDownloadUrl(@AuthenticationPrincipal User user,
                                           @PathVariable Long bookId,
                                           HttpServletRequest request) {
        return downloadService.generateDownloadUrl(bookId, user.getId(), request);
    }

    @GetMapping("/download")
    @Operation(summary = "Resolver descarga con token firmado (uso único)")
    public ResponseEntity<Resource> download(@RequestParam String token) {
        Download dl   = downloadService.resolveDownload(token);
        String   path = dl.getBook().getFilePath();

        if (path == null || path.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        File file = new File(path);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
}
