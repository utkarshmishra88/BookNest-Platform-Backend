package com.booknest.catalog.resource;

import com.booknest.catalog.entity.Book;
import com.booknest.catalog.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookResource {

    private final BookService bookService;

    // PUBLIC: Browse Catalog (No Token Needed once SecurityConfig is updated)
    @GetMapping("/public")
    public ResponseEntity<Page<Book>> getPublicCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        return ResponseEntity.ok(bookService.getAllBooksPaginated(page, size, sortBy, direction));
    }

    // ADMIN: Add Book
    @PostMapping("/category/{categoryId}")
    public ResponseEntity<Book> addBook(@PathVariable Long categoryId, @RequestBody Book book) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(categoryId, book));
    }

    // ADMIN: Get Single Book
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // ADMIN: Delete Book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
 // UC12: View Details of a Single Book
    @GetMapping("/public/{id}")
    public ResponseEntity<Book> getBookDetails(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getPublicBookDetails(id));
    }

    // UC13: Search & Filter Endpoint
    @GetMapping("/public/search")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        return ResponseEntity.ok(bookService.searchBooks(keyword, categoryId, page, size, sortBy, direction));
    }
}