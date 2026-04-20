package com.booknest.auth.service.impl;

import com.booknest.auth.dto.AddressResponse;
import com.booknest.auth.dto.AddressUpsertRequest;
import com.booknest.auth.entity.Address;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.AddressRepository;
import com.booknest.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User validUser;
    private Address validAddress;
    private AddressUpsertRequest upsertRequest;

    @BeforeEach
    void setUp() {
        validUser = User.builder().userId(1).build();
        validAddress = Address.builder()
                .addressId(10)
                .user(validUser)
                .line1("Line 1")
                .city("City")
                .state("State")
                .postalCode("12345")
                .country("Country")
                .mobileNumber("+919876543210")
                .isDefault(true)
                .build();

        upsertRequest = new AddressUpsertRequest();
        upsertRequest.setUserId(1);
        upsertRequest.setLine1("Line 1");
        upsertRequest.setCity("City");
        upsertRequest.setState("State");
        upsertRequest.setPostalCode("12345");
        upsertRequest.setCountry("Country");
        upsertRequest.setMobileNumber("+919876543210");
        upsertRequest.setIsDefault(true);
    }

    @Test
    void testGetUserAddresses() {
        when(addressRepository.findByUserUserIdOrderByIsDefaultDescAddressIdDesc(1))
                .thenReturn(List.of(validAddress));

        List<AddressResponse> addresses = addressService.getUserAddresses(1);

        assertEquals(1, addresses.size());
        assertEquals(10, addresses.get(0).getAddressId());
    }

    @Test
    void testCreateAddress_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(addressRepository.findByUserUserIdOrderByIsDefaultDescAddressIdDesc(1))
                .thenReturn(List.of(validAddress)); // to simulate clearing default
        when(addressRepository.save(any(Address.class))).thenReturn(validAddress);

        AddressResponse response = addressService.createAddress(upsertRequest);

        assertEquals(10, response.getAddressId());
        verify(addressRepository, times(1)).saveAll(any());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void testCreateAddress_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> addressService.createAddress(upsertRequest));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUpdateAddress_Success() {
        when(addressRepository.findById(10)).thenReturn(Optional.of(validAddress));
        when(addressRepository.findByUserUserIdOrderByIsDefaultDescAddressIdDesc(1))
                .thenReturn(List.of(validAddress)); // to simulate clearing default
        when(addressRepository.save(any(Address.class))).thenReturn(validAddress);

        AddressResponse response = addressService.updateAddress(10, upsertRequest);

        assertEquals(10, response.getAddressId());
        verify(addressRepository, times(1)).saveAll(any());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void testUpdateAddress_NotBelongToUser() {
        User otherUser = User.builder().userId(2).build();
        validAddress.setUser(otherUser);
        when(addressRepository.findById(10)).thenReturn(Optional.of(validAddress));

        Exception exception = assertThrows(RuntimeException.class, () -> addressService.updateAddress(10, upsertRequest));
        assertEquals("Address does not belong to user", exception.getMessage());
    }
}
