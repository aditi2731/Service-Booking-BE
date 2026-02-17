package com.example.ServiceBooking.providermanagement;



import com.example.ServiceBooking.auth.User;
import com.example.ServiceBooking.auth.UserRepository;
//import com.example.ServiceBooking.booking.*;
//import com.example.ServiceBooking.catalog.SubService;
//import com.example.ServiceBooking.catalog.SubServiceRepository;
//import com.example.ServiceBooking.provider.dto.*;
//import com.example.ServiceBooking.provider.entity.*;
//import com.example.ServiceBooking.provider.repository.*;
import com.example.ServiceBooking.bookings.Booking;
import com.example.ServiceBooking.bookings.BookingRepository;
import com.example.ServiceBooking.bookings.dto.BookingStatus;
import com.example.ServiceBooking.notification.NotificationService;
import com.example.ServiceBooking.providermanagement.dto.DocumentRequest;
import com.example.ServiceBooking.providermanagement.dto.EarningsResponse;
import com.example.ServiceBooking.providermanagement.dto.ProviderSetupRequest;
import com.example.ServiceBooking.servicecatalog.SubService;
import com.example.ServiceBooking.servicecatalog.SubServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PService {

    private final ProviderProfileRepository profileRepo;
    private final ProviderDocumentRepository docRepo;
    private final ProviderServiceRepository providerServiceRepo;
    private final SubServiceRepository subServiceRepo;
    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final ProviderAvailabilityRepository availabilityRepo;

    // Setup provider after registration
    public void setupProvider(Long userId, ProviderSetupRequest request) {
        log.trace("Entering setupProvider method");
        log.debug("Processing provider setup");
        log.info("Setting up provider profile");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        ProviderProfile profile = ProviderProfile.builder()
                .user(user)
                .approved(false)
                .online(false)
                .createdAt(LocalDateTime.now())
                .build();

        profileRepo.save(profile);

//        ProviderProfile profile = new ProviderProfile();
//        profile.setUser(user);
//        profile.setApproved(false);
//        profile.setOnline(false);
//        profile.setCreatedAt(LocalDateTime.now());
//
//        profileRepo.save(profile);


        for (Long subServiceId : request.getSubServiceIds()) {
            SubService subService = subServiceRepo.findById(subServiceId)
                    .orElseThrow(() -> {
                        log.error("SubService not found");
                        return new RuntimeException("SubService not found");
                    });

            ProviderService mapping = ProviderService.builder()
                    .provider(profile)
                    .subService(subService)
                    .build();

            providerServiceRepo.save(mapping);

            //Notification Integration
            notificationService.notifyAllAdmins("New provider awaiting approval");

        }

        log.debug("Provider setup completed successfully");
    }

    public void uploadDocument(Long userId, DocumentRequest request) {
        log.trace("Entering uploadDocument method");
        log.debug("Processing document upload");
        log.info("Uploading provider document");

        ProviderProfile profile = profileRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("Provider not found");
                    return new RuntimeException("Provider not found");
                });

        ProviderDocument doc = ProviderDocument.builder()
                .documentType(request.getDocumentType())
                .documentUrl(request.getDocumentUrl())
                .provider(profile)
                .build();

        docRepo.save(doc);

        //Notification Integration
        User providerUser = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        String providerName = providerUser.getName();

        notificationService.notifyAllAdmins("Provider " + providerName + " uploaded new document");

        log.debug("Document uploaded successfully");
    }

    public void toggleAvailability(Long userId, boolean online) {
        log.trace("Entering toggleAvailability method");
        log.debug("Processing availability toggle");
        log.info("Toggling provider availability");

        ProviderProfile profile = profileRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("Provider not found");
                    return new RuntimeException("Provider not found");
                });

        if (!profile.isApproved()) {
            log.error("Provider not approved");
            throw new RuntimeException("Provider not approved");
        }

        profile.setOnline(online);
        profileRepo.save(profile);

        log.debug("Provider availability toggled successfully");
    }

//    public EarningsResponse getEarnings(Long userId) {
//
//        List<Booking> completed =
//                bookingRepo.findByProviderIdAndStatus(
//                        userId, BookingStatus.COMPLETED);
//
//        double total = completed.stream()
//                .mapToDouble(b -> b.getServiceId().getBasePrice())
//                .sum();
//
//        return new EarningsResponse(completed.size(), total);
//    }

    public EarningsResponse getEarnings(Long userId) {
        log.trace("Entering getEarnings method");
        log.debug("Processing earnings calculation");
        log.info("Calculating provider earnings");

        List<Booking> completed =
                bookingRepo.findByProviderIdAndStatus(
                        userId, BookingStatus.COMPLETED);

        double total = completed.stream()
                .map(Booking::getPrice)
                .filter(p -> p != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        log.debug("Provider earnings calculated successfully");
        return new EarningsResponse(completed.size(), total);
    }

    // Providers can view available jobs (pending bookings) not the geographical logic
    public List<Booking> viewAvailableJobs(Long providerId) {
        log.trace("Entering viewAvailableJobs method");
        log.debug("Processing view available jobs request");
        log.info("Fetching available jobs for provider");

        ProviderProfile profile = profileRepo.findById(providerId)
                .orElseThrow(() -> {
                    log.error("Provider not found");
                    return new RuntimeException("Provider not found");
                });

        if (!profile.isApproved() || !profile.isOnline()) {
            log.error("Provider not available");
            throw new RuntimeException("Provider not available");
        }

        List<Booking> jobs = bookingRepo.findByStatus(BookingStatus.PENDING);
        log.debug("Available jobs retrieved successfully");
        return jobs;
    }

    // Accept the booking
    @Transactional
    public void acceptJob(Long providerId, Long bookingId) {
        log.trace("Entering acceptJob method");
        log.debug("Processing job acceptance");
        log.info("Provider accepting job");

        ProviderProfile profile = profileRepo.findById(providerId)
                .orElseThrow(() -> {
                    log.error("Provider not found");
                    return new RuntimeException("Provider not found");
                });

        if (!profile.isApproved() || !profile.isOnline()) {
            log.error("Provider not available");
            throw new RuntimeException("Provider not available");
        }


        int updatedRows = bookingRepo.assignIfPending(
                bookingId,
                providerId,
                BookingStatus.PENDING,
                BookingStatus.ACCEPTED
        );

        if (updatedRows == 0) {
            log.warn("Booking already accepted by another provider");
            throw new RuntimeException("Booking already accepted by another provider");
        }

         // Notification service Integration
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found");
                    return new RuntimeException("Booking not found");
                });

        User providerUser = userRepo.findById(providerId)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        String providerName = providerUser.getName();

        // Update the SAME notification row for this booking
        //Customer notification
        notificationService.upsertBookingNotification(
                booking.getCustomerId(),
                booking.getId(),
                providerName + " will service your booking"
        );

        // Provider notification
        notificationService.upsertBookingNotification(
                providerId,
                booking.getId(),
                "You accepted Booking #" + booking.getId()
        );

        log.debug("Job accepted successfully");
    }

    // Reject the booking
    public void rejectJob(Long providerId, Long bookingId) {
        log.trace("Entering rejectJob method");
        log.debug("Processing job rejection");
        log.info("Provider rejecting job");

        Booking booking = bookingRepo.findByIdAndStatus(
                        bookingId, BookingStatus.PENDING)
                .orElseThrow(() -> {
                    log.error("Booking not available");
                    return new RuntimeException("Booking not available");
                });

        // For now, we do nothing.
        // You can later add rejection tracking table.
        log.debug("Job rejection processed");
    }

    // Update job status to COMPLETED or CANCELLED
    public void updateJobStatus(Long providerId,
                                Long bookingId,
                                BookingStatus status) {
        log.trace("Entering updateJobStatus method");
        log.debug("Processing job status update");
        log.info("Updating job status");

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found");
                    return new RuntimeException("Booking not found");
                });

        if (!booking.getProviderId().equals(providerId)) {
            log.error("Not authorized to update this booking");
            throw new RuntimeException("Not your booking");
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            log.error("Invalid booking state for status update");
            throw new RuntimeException("Invalid booking state");
        }

        if (status != BookingStatus.COMPLETED &&
                status != BookingStatus.CANCELLED) {
            log.error("Invalid status update requested");
            throw new RuntimeException("Invalid status update");
        }

        booking.setStatus(status);
        bookingRepo.save(booking);



        // Notification service Integration
        if (status == BookingStatus.COMPLETED) {

            //  CUSTOMER: Job Completed (ask to rate)
            notificationService.upsertBookingNotification(
                    booking.getCustomerId(),
                    booking.getId(),
                    "Your service is completed. Please rate your provider."
            );

            // PROVIDER: Completed confirmation (optional but useful)
            notificationService.upsertBookingNotification(
                    booking.getProviderId(),
                    booking.getId(),
                    "You marked Booking #" + booking.getId() + " as completed."
            );

        } else if (status == BookingStatus.CANCELLED) {

            // If provider cancels from this method, notify customer
            notificationService.upsertBookingNotification(
                    booking.getCustomerId(),
                    booking.getId(),
                    "Your booking was cancelled"
            );

            // provider also gets a self-confirmation
            notificationService.upsertBookingNotification(
                    booking.getProviderId(),
                    booking.getId(),
                    "You cancelled Booking #" + booking.getId()
            );

        } else {

            //  generic status update (only if you ever allow other statuses later)
            notificationService.upsertBookingNotification(
                    booking.getCustomerId(),
                    booking.getId(),
                    "Booking status updated."
            );
        }

        log.debug("Job status updated successfully");
    }


//Slot Booking feature
@Transactional
public void setAvailabilityWindow(Long providerId, LocalDate date, LocalTime start, LocalTime end) {

    if (!start.isBefore(end)) throw new RuntimeException("startTime must be before endTime");

    ProviderProfile profile = profileRepo.findById(providerId)
            .orElseThrow(() -> new RuntimeException("Provider not found"));

    if (!profile.isApproved() || !profile.isOnline()) {
        throw new RuntimeException("Provider not available");
    }

    // remove old slots for that day (idempotent)
    availabilityRepo.deleteByProviderIdAndDate(providerId, date);

    var slots = new ArrayList<ProviderAvailability>();

    LocalTime t = start;
    while (t.plusHours(1).compareTo(end) <= 0) {
        ProviderAvailability s = new ProviderAvailability();
        s.setProviderId(providerId);
        s.setDate(date);
        s.setStartTime(t);
        s.setEndTime(t.plusHours(1));
        s.setStatus(AvailabilityStatus.AVAILABLE);
        slots.add(s);
        t = t.plusHours(1);
    }

    availabilityRepo.saveAll(slots);

}


}

