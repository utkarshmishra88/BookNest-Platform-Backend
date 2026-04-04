package com.booknest.catalog.service;

import com.booknest.catalog.entity.Book;
import com.booknest.catalog.entity.Category;
import com.booknest.catalog.repository.BookRepository;
import com.booknest.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    // Admin: Add a book to a specific category
    public Book addBook(Long categoryId, Book book) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
        book.setCategory(category);
        return bookRepository.save(book);
    }

    // Public: Get books with Pagination and Sorting
    public Page<Book> getAllBooksPaginated(int page, int size, String sortBy, String direction) {
        // Determine sorting direction based on the string passed from the controller
        Sort sort = direction.equalsIgnoreCase("desc") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending();
        
        // Create the Pageable request object
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Let Spring Data JPA do the heavy lifting
        return bookRepository.findAll(pageable);
    }

    // Admin: Get a single book
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    // Admin: Delete a book
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
    
 // UC12: Public Single Book Details
    public Book getPublicBookDetails(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    // UC13: The Search Engine
    public Page<Book> searchBooks(String keyword, Long categoryId, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Scenario 1: User clicked on a Category Filter
        if (categoryId != null) {
            return bookRepository.findByCategoryId(categoryId, pageable);
        } 
        // Scenario 2: User typed in the Search Bar
        else if (keyword != null && !keyword.trim().isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable);
        } 
        
        // Scenario 3: Empty search, just return all books
        return bookRepository.findAll(pageable);
    }
}