package com.booknest.catalog.repository;

import com.booknest.catalog.entity.Book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
	// UC13: Search by Category (e.g., SELECT * FROM books WHERE category_id = ?)
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    // UC13: Search by Keyword (e.g., SELECT * FROM books WHERE title LIKE %keyword% OR author LIKE %keyword%)
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author, Pageable pageable);
}