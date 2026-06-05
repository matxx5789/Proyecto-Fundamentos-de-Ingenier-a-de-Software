package com.openlib.service;

import com.openlib.domain.Book;
import com.openlib.domain.Download;
import com.openlib.dto.response.BookResponse;
import com.openlib.dto.response.DownloadResponse;
import com.openlib.exception.BusinessException;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.repository.BookRepository;
import com.openlib.repository.DownloadRepository;
import com.openlib.service.BookServicePort;
import com.openlib.util.SignedUrlGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestiona la biblioteca personal del usuario y las descargas con Signed URLs.
 *
 * Flujo:
 * 1. El usuario completa un checkout → OrderService crea un Download por cada libro.
 * 2. El usuario llama a GET /library         → getLibrary() lista sus libros adquiridos.
 * 3. El usuario llama a GET /library/{id}/download-url → generateDownloadUrl() genera token HMAC.
 * 4. El usuario llama a GET /library/download?token=XXX → resolveDownload() valida y marca usado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadService {

    private final DownloadRepository  downloadRepository;
    private final BookRepository      bookRepository;
    private final SignedUrlGenerator  signedUrlGenerator;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    // ── Biblioteca personal ───────────────────────────────────────────────────

    /**
     * Retorna la lista de libros adquiridos por el usuario.
     * Delega el mapeo a BookResponse en BookService para reutilizar la lógica existente.
     */
    public List<BookResponse> getLibrary(Long userId, BookServicePort bookService) {
        return downloadRepository.findByUserId(userId)
                .stream()
                .map(dl -> bookService.toResponse(dl.getBook()))
                .distinct()
                .toList();
    }

    // ── Generación de Signed URL ──────────────────────────────────────────────

    /**
     * Genera un Signed URL de descarga para un libro adquirido.
     * El token expira en 5 minutos y es de uso único.
     *
     * @param bookId  ID del libro a descargar
     * @param userId  ID del usuario autenticado
     * @param request petición HTTP (para construir la URL base)
     * @return DownloadResponse con la URL firmada y su fecha de expiración
     */
    @Transactional
    public DownloadResponse generateDownloadUrl(Long bookId, Long userId, HttpServletRequest request) {
        // Verificar que el usuario tiene acceso al libro (lo adquirió)
        boolean hasAccess = downloadRepository.existsByUserIdAndBookId(userId, bookId);
        if (!hasAccess) {
            throw new BusinessException(
                    "No tienes acceso a este libro. Debes comprarlo primero.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", bookId));

        // Generar token firmado HMAC-SHA256
        String token     = signedUrlGenerator.generateToken(userId, bookId);
        LocalDateTime exp = signedUrlGenerator.getExpiration();

        // Persistir el token en el registro de descarga más reciente del usuario para este libro
        downloadRepository.findByUserId(userId).stream()
                .filter(dl -> dl.getBook().getId().equals(bookId))
                .findFirst()
                .ifPresent(dl -> {
                    dl.setSignedToken(token);
                    dl.setTokenExpires(exp);
                    downloadRepository.save(dl);
                });

        // Construir la URL de descarga
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + contextPath;
        String downloadUrl = baseUrl + "/library/download?token=" + token;

        log.info("Signed URL generada para userId={} bookId={} expira={}", userId, bookId, exp);

        return new DownloadResponse(bookId, book.getTitle(), downloadUrl, exp);
    }

    // ── Resolución de descarga ────────────────────────────────────────────────

    /**
     * Valida el token firmado, marca la descarga como realizada y retorna el Download.
     * El token se invalida después del primer uso.
     *
     * @param token  token firmado recibido por query param
     * @return Download con la información del libro y la ruta del archivo
     */
    @Transactional
    public Download resolveDownload(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Token de descarga inválido o ausente.");
        }

        // Validar firma y expiración
        if (!signedUrlGenerator.isValid(token)) {
            throw new BusinessException(
                    "El token de descarga ha expirado o es inválido. Genera uno nuevo.");
        }

        // Buscar el registro de descarga asociado al token
        Download dl = downloadRepository.findBySignedToken(token)
                .orElseThrow(() -> new BusinessException(
                        "Token de descarga no reconocido. Genera uno nuevo desde tu biblioteca."));

        // Verificar que no fue usado antes (uso único)
        if (dl.getDownloadedAt() != null) {
            throw new BusinessException(
                    "Este token ya fue utilizado. Genera un nuevo enlace de descarga.");
        }

        // Registrar la descarga
        dl.setDownloadedAt(LocalDateTime.now());
        dl.setSignedToken(null);   // invalidar para que no pueda reutilizarse
        dl.setTokenExpires(null);
        downloadRepository.save(dl);

        // Incrementar contador de descargas del libro
        Book book = dl.getBook();
        book.setDownloadCount(
                book.getDownloadCount() + 1L);
        bookRepository.save(book);

        log.info("Descarga completada: userId={} bookId={} titulo={}",
                dl.getUser().getId(), book.getId(), book.getTitle());

        return dl;
    }
}
