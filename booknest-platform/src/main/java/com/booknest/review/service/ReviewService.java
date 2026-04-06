package com.booknest.review.service;

import com.booknest.review.entity.Review;
import com.booknest.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Review addReview(Review review) { // [cite: 496]
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5 stars");
        }
        
        Optional<Review> existingReview = reviewRepository.findByBookIdAndUserId(review.getBookId(), review.getUserId());
        if (existingReview.isPresent()) {
            throw new RuntimeException("User has already reviewed this book!");
        }

        review.setReviewDate(LocalDate.now());
        // Note: In a fully distributed system, we would use a RestTemplate here to call the Order-Service 
        // to verify if the user purchased the book. For now, we accept the incoming boolean.
        return reviewRepository.save(review);
    }

    public List<Review> getByBook(Integer bookId) { // [cite: 497]
        return reviewRepository.findByBookId(bookId);
    }

    public List<Review> getByUser(Integer userId) { // [cite: 498]
        return reviewRepository.findByUserId(userId);
    }

    public Review updateReview(Integer reviewId, Review updatedReview) { // [cite: 499]
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        review.setRating(updatedReview.getRating());
        review.setComment(updatedReview.getComment());
        review.setReviewDate(LocalDate.now()); // Update timestamp
        
        return reviewRepository.save(review);
    }

    public void deleteReview(Integer reviewId) { // [cite: 500]
        reviewRepository.deleteById(reviewId);
    }

    public Double getAvgRating(Integer bookId) { // [cite: 501]
        return reviewRepository.avgRatingByBookId(bookId);
    }
}