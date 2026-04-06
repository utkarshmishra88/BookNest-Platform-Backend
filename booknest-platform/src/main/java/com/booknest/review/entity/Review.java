package com.booknest.review.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "reviews")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId; // Primary Key [cite: 458]

    @Column(nullable = false)
    private Integer bookId; // Storing the ID instead of a @ManyToOne relationship [cite: 459]

    @Column(nullable = false)
    private Integer userId; // Storing the ID instead of a @ManyToOne relationship [cite: 460]

    @Column(nullable = false)
    private Integer rating; // 1 to 5 stars [cite: 461]

    @Column(length = 1000)
    private String comment; // Text review [cite: 462]

    private LocalDate reviewDate; // Date of review [cite: 463]

    private Boolean verified; // True if the user actually purchased the book [cite: 464]
}