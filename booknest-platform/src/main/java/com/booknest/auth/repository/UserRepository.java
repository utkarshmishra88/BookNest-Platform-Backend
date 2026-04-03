package com.booknest.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.booknest.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
	
	//Exclude nullpointer exception
	Optional<User> findByEmail(String email);
	
	boolean existsByEmail(String email);

}
