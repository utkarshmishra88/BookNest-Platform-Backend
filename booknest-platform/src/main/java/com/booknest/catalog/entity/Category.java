package com.booknest.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;
}

//Purpose: This is the "Blueprints" layer. It defines how the Java object maps to the MySQL table.
//
//Key Takeaways:
//
//@Entity: Tells Hibernate this class isn't just a regular Java class; it’s a database table.
//
//unique = true: A database-level guard. It prevents two categories from having the same name (e.g., you can't have two "Java" categories).
//
//length = 500: Increases the default string limit (255) to allow for longer descriptions.