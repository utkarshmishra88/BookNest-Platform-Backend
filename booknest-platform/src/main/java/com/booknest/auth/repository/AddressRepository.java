package com.booknest.auth.repository;

import com.booknest.auth.entity.Address;
import com.booknest.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Custom query to find all addresses for a specific user
    List<Address> findByUser(User user);
}