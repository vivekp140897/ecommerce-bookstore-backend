package com.bookstore.service;

import com.bookstore.dto.request.AddressRequest;
import com.bookstore.dto.response.AddressResponse;
import com.bookstore.entity.Address;
import com.bookstore.entity.User;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.AddressRepository;
import com.bookstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final SecurityUtils securityUtils;

    public List<AddressResponse> listAddresses() {
        User user = securityUtils.getCurrentUser();
        return addressRepository.findByUser(user).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        User user = securityUtils.getCurrentUser();
        Address address = Address.builder()
                .user(user)
                .label(request.label())
                .line1(request.line1())
                .line2(request.line2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .isDefault(request.isDefault() != null && request.isDefault())
                .build();
        return toResponse(addressRepository.save(address));
    }

    public AddressResponse getAddress(UUID id) {
        User user = securityUtils.getCurrentUser();
        return toResponse(findOwned(id, user));
    }

    @Transactional
    public AddressResponse updateAddress(UUID id, AddressRequest request) {
        User user = securityUtils.getCurrentUser();
        Address address = findOwned(id, user);
        address.setLabel(request.label());
        address.setLine1(request.line1());
        address.setLine2(request.line2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        address.setIsDefault(request.isDefault() != null && request.isDefault());
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UUID id) {
        User user = securityUtils.getCurrentUser();
        Address address = findOwned(id, user);
        addressRepository.delete(address);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Address findOwned(UUID id, User user) {
        return addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
    }

    public AddressResponse toResponse(Address a) {
        return new AddressResponse(a.getId(), a.getUser().getId(), a.getLabel(),
                a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
                a.getPostalCode(), a.getCountry(), a.getIsDefault(), a.getCreatedAt());
    }
}
