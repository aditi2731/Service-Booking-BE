package com.example.ServiceBooking.customer;

import com.example.ServiceBooking.auth.User;
import com.example.ServiceBooking.auth.UserRepository;
import com.example.ServiceBooking.customer.dto.AddressRequest;
import com.example.ServiceBooking.customer.dto.AddressResponse;
import com.example.ServiceBooking.customer.dto.CustomerProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository profileRepo;
    private final AddressRepository addressRepo;
    private final UserRepository userRepo;

    // Get profile
    public CustomerProfileResponse getProfile(Long userId) {
        log.trace("Entering getProfile method");
        log.debug("Fetching customer profile");
        log.info("Retrieving customer profile");

        CustomerProfile profile = profileRepo.findById(userId)
                .orElseGet(() -> createProfile(userId));

        log.debug("Customer profile retrieved successfully");
        return mapProfile(profile);
    }

    // Update profile
    public void updateProfile(Long userId, String name) {
        log.trace("Entering updateProfile method");
        log.debug("Processing profile update");
        log.info("Updating customer profile");

        CustomerProfile profile = profileRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("Profile not found");
                    return new RuntimeException("Profile not found");
                });

        profile.setName(name);
        profileRepo.save(profile);
        log.debug("Customer profile updated successfully");
    }

    // Add address
    public void addAddress(Long userId, AddressRequest request) {
        log.trace("Entering addAddress method");
        log.debug("Processing add address");
        log.info("Adding customer address");

        CustomerProfile profile = profileRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("Profile not found");
                    return new RuntimeException("Profile not found");
                });

        if (request.isDefault()) {
            log.debug("Unsetting other default addresses");
            unsetDefault(profile);
        }

        Address address = new Address();
        address.setAddressLine(request.addressLine());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPincode(request.pincode());
        address.setDefaultAddress(request.isDefault());
        address.setCustomer(profile);

        addressRepo.save(address);
        log.debug("Address added successfully");
    }

    //  Update address
    public void updateAddress(Long addressId, AddressRequest request, Long userId) {
        log.trace("Entering updateAddress method");
        log.debug("Processing update address");
        log.info("Updating customer address");

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> {
                    log.error("Address not found");
                    return new RuntimeException("Address not found");
                });

        if (!address.getCustomer().getCustomerId().equals(userId)) {
            log.error("Unauthorized address update attempt");
            throw new RuntimeException("Unauthorized");
        }

        if (request.isDefault()) {
            log.debug("Unsetting other default addresses");
            unsetDefault(address.getCustomer());
        }

        address.setAddressLine(request.addressLine());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPincode(request.pincode());
        address.setDefaultAddress(request.isDefault());

        addressRepo.save(address);
        log.debug("Address updated successfully");
    }

    // Delete address
    public void deleteAddress(Long addressId, Long userId) {
        log.trace("Entering deleteAddress method");
        log.debug("Processing delete address");
        log.info("Deleting customer address");

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> {
                    log.error("Address not found");
                    return new RuntimeException("Address not found");
                });

        if (!address.getCustomer().getCustomerId().equals(userId)) {
            log.error("Unauthorized address deletion attempt");
            throw new RuntimeException("Unauthorized");
        }

        addressRepo.delete(address);
        log.debug("Address deleted successfully");
    }

    // helpers
    private void unsetDefault(CustomerProfile profile) {
        profile.getAddresses().forEach(a -> a.setDefaultAddress(false));
    }

    private CustomerProfile createProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerId(user.getId());
        profile.setName(user.getName());
        profile.setEmail(user.getEmail());

        return profileRepo.save(profile);
    }

    private CustomerProfileResponse mapProfile(CustomerProfile profile) {
        return new CustomerProfileResponse(
                profile.getCustomerId(),
                profile.getName(),
                profile.getEmail(),
                profile.getAddresses()
                        .stream()
                        .map(a -> new AddressResponse(
                                a.getId(),
                                a.getAddressLine(),
                                a.getCity(),
                                a.getState(),
                                a.getPincode(),
                                a.isDefaultAddress()
                        ))
                        .toList()
        );
    }
}

