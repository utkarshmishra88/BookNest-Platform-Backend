package com.booknest.auth.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Maps directly to our user table.
//Lombok removes boilerplate code.
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	
	@NotBlank(message = "Full name is required.")
	private String fullName;
	
	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	@Column(unique=true, nullable=false)
	private String email;
	
	@JsonIgnore
	@NotBlank(message = "Password is required")
	private String passwordHash;
	
	private String role;
	
	private String provider;
	
	private Long mobile;
	
	private LocalDateTime createdAt;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Address> addresses;

}
