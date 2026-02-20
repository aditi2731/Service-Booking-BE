package com.example.ServiceBooking.bookings;

import com.example.ServiceBooking.auth.User;
import com.example.ServiceBooking.auth.UserRepository;
import com.example.ServiceBooking.bookings.dto.*;
import com.example.ServiceBooking.notification.NotificationService;
import com.example.ServiceBooking.providermanagement.*;
import com.example.ServiceBooking.servicecatalog.SubService;
import com.example.ServiceBooking.servicecatalog.SubServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository repo;
    private final NotificationService notificationService;
    private final SubServiceRepository subServiceRepo;
    private final ProviderAvailabilityRepository availabilityRepo;
    private final ProviderServiceRepository providerServiceRepo;
    private final ProviderProfileRepository providerProfileRepo;
    private final UserRepository userRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();




    //  ========================
    //  Get nearby providers based on customer's city and optional service filter3
    //  ========================
    public List<NearbyProviderResponse> getNearbyProviders(Long customerId, Long serviceId) {
        log.trace("Entering getNearbyProviders method");
        log.info("Fetching nearby providers");

        String city = resolveUserCity(customerId);

        List<Long> providerIds = (serviceId != null)
                ? providerProfileRepo.findEligibleProviderIdsByCityAndService(city, serviceId)
                : providerProfileRepo.findEligibleProviderIdsByCityOnly(city);

        if (providerIds.isEmpty()) {
            log.warn("No providers found");
            return List.of();
        }

        // Load provider names (from users table)
        // If you want fewer queries, you can create a query joining user + profile;
        // but this works fine for now.
        log.debug("Nearby providers retrieved successfully");
        return providerIds.stream()
                .map(pid -> {
                    String name = userRepo.findById(pid).map(User::getName).orElse("Provider");
                    return new NearbyProviderResponse(pid, name, city);
                })
                .toList();
    }


    // =========================
    // CREATE BOOKING (PENDING)
    // =========================
    public void createBooking(Long customerId, BookingCreateRequest req) {
        log.trace("Entering createBooking method");
        log.info("Creating new booking");

        SubService subService = subServiceRepo.findById(req.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setServiceId(req.serviceId());
        booking.setDateTime(req.dateTime());
        booking.setLocation(req.location());
        booking.setCity(resolveUserCity(customerId));
        booking.setPrice(subService.getBasePrice());
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = repo.save(booking);

        notificationService.upsertBookingNotification(
                customerId,
                savedBooking.getId(),
                "Booking confirmed successfully."
        );

        log.debug("Booking created successfully");
    }

    // ==================
    // CANCEL BOOKING
    // ==================
    public void cancelBooking(Long bookingId, Long userId) {
        log.trace("Entering cancelBooking method");
        log.info("Cancelling booking");

        Booking booking = getBooking(bookingId);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            log.error("Cannot cancel a completed booking");
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        if (!userId.equals(booking.getCustomerId())
                && (booking.getProviderId() == null || !userId.equals(booking.getProviderId()))) {
            log.error("Unauthorized cancellation");
            throw new RuntimeException("Unauthorized cancellation");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        repo.save(booking);

        if (userId.equals(booking.getCustomerId())) {
            notificationService.upsertBookingNotification(
                    booking.getCustomerId(),
                    booking.getId(),
                    "Your booking was cancelled"
            );

            if (booking.getProviderId() != null) {
                notificationService.upsertBookingNotification(
                        booking.getProviderId(),
                        booking.getId(),
                        "Booking #" + booking.getId() + " was cancelled by customer"
                );
            }
        } else {
            notificationService.upsertBookingNotification(
                    booking.getCustomerId(),
                    booking.getId(),
                    "Your booking was cancelled"
            );

            notificationService.upsertBookingNotification(
                    booking.getProviderId(),
                    booking.getId(),
                    "You cancelled Booking #" + booking.getId()
            );
        }

        log.debug("Booking cancelled successfully");
    }

    // ==================
    // LISTS
    // ==================
    public List<BookingResponse> customerBookings(Long customerId) {
        log.trace("Entering customerBookings method");
        log.info("Fetching customer bookings");
        log.debug("Customer bookings retrieved successfully");
        return repo.findByCustomerId(customerId).stream().map(this::map).toList();
    }

    public List<BookingResponse> providerBookings(Long providerId) {
        log.trace("Entering providerBookings method");
        log.info("Fetching provider bookings");
        log.debug("Provider bookings retrieved successfully");
        return repo.findByProviderId(providerId).stream().map(this::map).toList();
    }

    public List<BookingResponse> history(Long userId, boolean isCustomer) {
        log.trace("Entering history method");
        log.info("Fetching booking history");
        log.debug("Booking history retrieved successfully");
        return isCustomer
                ? repo.findByCustomerIdOrderByCreatedAtDesc(userId).stream().map(this::map).toList()
                : repo.findByProviderIdOrderByCreatedAtDesc(userId).stream().map(this::map).toList();
    }

    private Booking getBooking(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    private BookingResponse map(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getCustomerId(),
                b.getProviderId(),
                b.getServiceId(),
                b.getDateTime(),
                b.getLocation(),
                b.getCity(),
                b.getStatus()
        );
    }

    // =========================
    // SLOTS FEATURE (EXISTING)
    // =========================
    public List<SlotResponse> getSlotsForService(Long serviceId, LocalDate date, String city) {
        log.trace("Entering getSlotsForService method");
        log.info("Fetching available slots for service");

        List<Long> providerIds = providerServiceRepo.findProviderIdsBySubServiceId(serviceId);
        if (providerIds.isEmpty()) {
            log.warn("No providers found for service");
            return List.of();
        }

        if (city == null || city.isBlank()) {
            log.error("City is required");
            throw new RuntimeException("City is required");
        }

        List<Long> eligibleInCity = providerProfileRepo.findEligibleProviderIdsByCity(city.trim());
        List<Long> eligibleProviders = providerIds.stream().filter(eligibleInCity::contains).toList();
        if (eligibleProviders.isEmpty()) {
            log.warn("No eligible providers found in city");
            return List.of();
        }

        List<ProviderAvailability> allSlots =
                availabilityRepo.findAvailableSlotsForProviders(eligibleProviders, date);

        Map<String, Long> grouped = allSlots.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getStartTime() + "-" + s.getEndTime(),
                        java.util.stream.Collectors.counting()
                ));

        log.debug("Slots retrieved successfully");
        return grouped.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("-");
                    return new SlotResponse(LocalTime.parse(parts[0]), LocalTime.parse(parts[1]), e.getValue());
                })
                .sorted(java.util.Comparator.comparing(SlotResponse::startTime))
                .toList();
    }

    // =========================
    // BOOK SLOT (ACCEPTED)
    // =========================
    @Transactional
    public BookingResponse bookSlot(Long customerId, SlotBookingRequest req) {
        log.trace("Entering bookSlot method");
        log.info("Booking slot for customer");

        LocalDate date = req.dateTime().toLocalDate();
        LocalTime start = req.dateTime().toLocalTime();
        LocalTime end = start.plusHours(1);

        ProviderAvailability lockedSlot = null;
        Long providerId = req.providerId();
        String city = resolveUserCity(customerId);

        if (providerId != null) {
            ensureProviderCityMatch(providerId, city);
            lockedSlot = availabilityRepo.lockAvailableSlot(providerId, date, start, end)
                    .orElseThrow(() -> {
                        log.error("Slot not available");
                        return new RuntimeException("Slot not available");
                    });
        } else {
            List<Long> providerIds = providerServiceRepo.findProviderIdsBySubServiceId(req.serviceId());
            if (providerIds.isEmpty()) {
                log.error("No provider available for this service");
                throw new RuntimeException("No provider available for this service");
            }

            for (Long pid : providerIds) {
                boolean eligible = providerProfileRepo.findById(pid)
                        .map(p -> p.isApproved() && p.isOnline() && city.equalsIgnoreCase(p.getCity()))
                        .orElse(false);

                if (!eligible) continue;

                Optional<ProviderAvailability> opt = availabilityRepo.lockAvailableSlot(pid, date, start, end);
                if (opt.isPresent()) {
                    lockedSlot = opt.get();
                    providerId = pid;
                    break;
                }
            }

            if (lockedSlot == null) {
                log.error("No provider available for this slot");
                throw new RuntimeException("No provider available for this slot");
            }
        }

        // block overlaps for ACCEPTED + STARTED (job already in progress)
        boolean overlap = repo.existsByProviderIdAndDateTimeBetweenAndStatusIn(
                providerId,
                req.dateTime(),
                req.dateTime().plusMinutes(59),
                List.of(BookingStatus.ACCEPTED, BookingStatus.STARTED)
        );
        if (overlap) {
            log.error("Provider already booked for this slot");
            throw new RuntimeException("Provider already booked for this slot");
        }

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setProviderId(providerId);
        booking.setServiceId(req.serviceId());
        booking.setDateTime(req.dateTime());
        booking.setLocation(req.location());
        booking.setCity(city);
        booking.setStatus(BookingStatus.ACCEPTED);

        SubService subService = subServiceRepo.findById(req.serviceId())
                .orElseThrow(() -> {
                    log.error("Service not found");
                    return new RuntimeException("Service not found");
                });
        booking.setPrice(subService.getBasePrice());

        Booking saved = repo.save(booking);

        lockedSlot.setStatus(AvailabilityStatus.BOOKED);
        availabilityRepo.save(lockedSlot);

        // Generate & notify Start OTP (Urban Company flow)
        generateAndNotifyStartOtp(saved);

        notificationService.upsertBookingNotification(
                saved.getProviderId(),
                saved.getId(),
                "New Job: Booking #" + saved.getId() + ". Ask customer for OTP to start the job."
        );

        log.debug("Slot booked successfully");
        return map(saved);
    }

    // ======================================================
    // JOB START OTP FLOW (NEW METHODS)
    // ======================================================

    /**
     * CUSTOMER: Resend Start OTP (regenerates new OTP and notifies customer).
     */
    @Transactional
    public void resendStartOtp(Long bookingId, Long customerId) {
        log.trace("Entering resendStartOtp method");
        log.info("Resending start OTP");

        Booking booking = getBooking(bookingId);

        if (!customerId.equals(booking.getCustomerId())) {
            log.error("Unauthorized");
            throw new RuntimeException("Unauthorized");
        }

        if (booking.getProviderId() == null || booking.getStatus() != BookingStatus.ACCEPTED) {
            log.error("OTP can be resent only for ACCEPTED bookings with provider assigned");
            throw new RuntimeException("OTP can be resent only for ACCEPTED bookings with provider assigned");
        }

        // allow resend only if not verified yet
        if (booking.getStartOtpVerifiedAt() != null) {
            log.error("Job already started");
            throw new RuntimeException("Job already started. OTP already verified.");
        }

        generateAndNotifyStartOtp(booking);
        log.debug("Start OTP resent successfully");
    }

    /**
     * PROVIDER: Verify Start OTP before starting job.
     * If OTP matches → mark STARTED and store verification timestamp.
     */
    @Transactional
    public void verifyStartOtp(Long bookingId, Long providerId, String otp) {
        log.trace("Entering verifyStartOtp method");
        log.info("Verifying start OTP");

        Booking booking = getBooking(bookingId);

        if (booking.getProviderId() == null || !providerId.equals(booking.getProviderId())) {
            log.error("Unauthorized: This booking is not assigned to you");
            throw new RuntimeException("Unauthorized: This booking is not assigned to you");
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            log.error("OTP verification allowed only when booking is ACCEPTED");
            throw new RuntimeException("OTP verification allowed only when booking is ACCEPTED");
        }

        if (booking.getStartOtpHash() == null) {
            log.error("Start OTP not generated yet");
            throw new RuntimeException("Start OTP not generated yet");
        }

        if (booking.getStartOtpVerifiedAt() != null) {
            log.error("OTP already verified");
            throw new RuntimeException("OTP already verified. Job already started.");
        }

        boolean matches = encoder.matches(otp.trim(), booking.getStartOtpHash());
        if (!matches) {
            log.error("Invalid OTP");
            throw new RuntimeException("Invalid OTP");
        }

        booking.setStartOtpVerifiedAt(LocalDateTime.now());
        booking.setStatus(BookingStatus.STARTED);
        repo.save(booking);

        // notify both
        notificationService.upsertBookingNotification(
                booking.getCustomerId(),
                booking.getId(),
                "Your job has started for Booking #" + booking.getId()
        );

        notificationService.upsertBookingNotification(
                booking.getProviderId(),
                booking.getId(),
                "OTP verified. You can start the job for Booking #" + booking.getId()
        );

        log.debug("Start OTP verified successfully");
    }

    private void generateAndNotifyStartOtp(Booking booking) {
        String otp = generateNumericOtp(4);
        booking.setStartOtpHash(encoder.encode(otp));
        booking.setStartOtpGeneratedAt(LocalDateTime.now());
        repo.save(booking);

        // customer sees OTP in app via notifications (matches UC experience)
        notificationService.upsertBookingNotification(
                booking.getCustomerId(),
                booking.getId(),
                "Job Start OTP for Booking #" + booking.getId() + " is: " + otp + " (Share with provider to start the job)"
        );
    }

    private String generateNumericOtp(int digits) {
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        int val = random.nextInt(max - min + 1) + min;
        return String.valueOf(val);
    }

    // =========================
    // HELPERS
    // =========================
    private String resolveUserCity(Long userId) {
        String city = userRepo.findById(userId)
                .map(u -> u.getCity())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (city == null || city.isBlank()) throw new RuntimeException("User city not set");
        return city;
    }

    private void ensureProviderCityMatch(Long providerId, String city) {
        boolean matches = providerProfileRepo.findById(providerId)
                .map(p -> p.getCity() != null && p.getCity().equalsIgnoreCase(city))
                .orElse(false);
        if (!matches) throw new RuntimeException("Provider is in a different city");
    }
}
