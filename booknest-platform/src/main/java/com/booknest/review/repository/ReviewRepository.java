package com.booknest.review.repository;

import com.booknest.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    // Find all reviews for a specific book [cite: 477]
    List<Review> findByBookId(Integer bookId);
    
    // Find all reviews by a specific user [cite: 477]
    List<Review> findByUserId(Integer userId);
    
    // Check if user already reviewed this book (1 review per user per book) [cite: 478]
    Optional<Review> findByBookIdAndUserId(Integer bookId, Integer userId);

    // Calculate the average rating. COALESCE ensures it returns 0.0 instead of null if empty. [cite: 479]
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.bookId = :bookId")
    Double avgRatingByBookId(@Param("bookId") Integer bookId);
}