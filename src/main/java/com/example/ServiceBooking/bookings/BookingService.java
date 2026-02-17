package com.example.ServiceBooking.bookings;

import com.example.ServiceBooking.bookings.dto.*;
import com.example.ServiceBooking.notification.NotificationService;
import com.example.ServiceBooking.providermanagement.*;
import com.example.ServiceBooking.servicecatalog.SubService;
import com.example.ServiceBooking.servicecatalog.SubServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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


    // Create Booking (CUSTOMER)
    public void createBooking(Long customerId, BookingCreateRequest req) {
        log.trace("Entering createBooking method");
        log.debug("Processing booking creation");
        log.info("Creating booking for customer");

        SubService subService = subServiceRepo.findById(req.serviceId())
                .orElseThrow(() -> {
                    log.error("Service not found");
                    return new RuntimeException("Service not found");
                });

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setServiceId(req.serviceId());
        booking.setDateTime(req.dateTime());
        booking.setLocation(req.location());
        booking.setPrice(subService.getBasePrice());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        // Capture the saved entity
        Booking savedBooking = repo.save(booking);

        // Notification service Integration
        notificationService.upsertBookingNotification(
                customerId,
                savedBooking.getId(),
                "Booking confirmed successfully."
        );

        log.debug("Booking created successfully");
    }

    //  Cancel booking
    public void cancelBooking(Long bookingId, Long userId) {



        log.trace("Entering cancelBooking method");
        log.debug("Processing booking cancellation");
        log.info("Cancelling booking");

        Booking booking = getBooking(bookingId);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        if (!userId.equals(booking.getCustomerId())
                && !userId.equals(booking.getProviderId())) {
            log.error("Unauthorized cancellation attempt");
            throw new RuntimeException("Unauthorized cancellation");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        repo.save(booking);


        if (userId.equals(booking.getCustomerId())) {
            // CUSTOMER: Booking Cancelled (self)
            notificationService.upsertBookingNotification(
                    booking.getCustomerId(),
                    booking.getId(),
                    "Your booking was cancelled"
            );

            //  PROVIDER: Customer Cancelled (with booking id)
            if (booking.getProviderId() != null) {
                notificationService.upsertBookingNotification(
                        booking.getProviderId(),
                        booking.getId(),
                        "Booking #" + booking.getId() + " was cancelled by customer"
                );
            }
        } else {
            // Provider cancelled (optional messaging)
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

    // Customer bookings
    public List<BookingResponse> customerBookings(Long customerId) {
        log.trace("Entering customerBookings method");
        log.debug("Fetching customer bookings");
        log.info("Retrieving customer bookings");
        List<BookingResponse> bookings = repo.findByCustomerId(customerId).stream().map(this::map).toList();
        log.debug("Customer bookings retrieved successfully");
        return bookings;
    }

    // Provider bookings
    public List<BookingResponse> providerBookings(Long providerId) {
        log.trace("Entering providerBookings method");
        log.debug("Fetching provider bookings");
        log.info("Retrieving provider bookings");
        List<BookingResponse> bookings = repo.findByProviderId(providerId).stream().map(this::map).toList();
        log.debug("Provider bookings retrieved successfully");
        return bookings;
    }

    // Booking history
    public List<BookingResponse> history(Long userId, boolean isCustomer) {
        log.trace("Entering history method");
        log.debug("Fetching booking history");
        log.info("Retrieving booking history");
        List<BookingResponse> history = isCustomer
                ? repo.findByCustomerIdOrderByCreatedAtDesc(userId).stream().map(this::map).toList()
                : repo.findByProviderIdOrderByCreatedAtDesc(userId).stream().map(this::map).toList();
        log.debug("Booking history retrieved successfully");
        return history;
    }

    private Booking getBooking(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found");
                    return new RuntimeException("Booking not found");
                });
    }

    private BookingResponse map(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getCustomerId(),
                b.getProviderId(),
                b.getServiceId(),
                b.getDateTime(),
                b.getLocation(),
                b.getStatus()
        );
    }


    //slots Feature
    public List<SlotResponse> getSlotsForService(Long serviceId, LocalDate date) {

        List<Long> providerIds = providerServiceRepo.findProviderIdsBySubServiceId(serviceId);
        if (providerIds.isEmpty()) return List.of();

        List<Long> eligibleProviders = providerIds.stream()
                .filter(pid -> providerProfileRepo.findById(pid)
                        .map(p -> p.isApproved() && p.isOnline())
                        .orElse(false))
                .toList();

        if (eligibleProviders.isEmpty()) return List.of();

        List<ProviderAvailability> allSlots =
                availabilityRepo.findAvailableSlotsForProviders(eligibleProviders, date);

        Map<String, Long> grouped = allSlots.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getStartTime() + "-" + s.getEndTime(),
                        java.util.stream.Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("-");
                    return new SlotResponse(LocalTime.parse(parts[0]), LocalTime.parse(parts[1]), e.getValue());
                })
                .sorted(java.util.Comparator.comparing(SlotResponse::startTime))
                .toList();
    }

    // book slots
//    @Transactional
//    public BookingResponse bookSlot(Long customerId, SlotBookingRequest req) {
//
//        LocalDate date = req.dateTime().toLocalDate();
//        LocalTime start = req.dateTime().toLocalTime();
//        LocalTime end = start.plusHours(1);
//
//        Long providerId = req.providerId();
//        if (providerId == null) {
//            providerId = pickProvider(req.serviceId(), date, start, end);
//            if (providerId == null) throw new RuntimeException("No provider available for this slot");
//        }
//
//        ProviderAvailability slot = availabilityRepo.lockAvailableSlot(providerId, date, start, end)
//                .orElseThrow(() -> new RuntimeException("Slot not available"));
//
//        // create booking directly as ACCEPTED because provider + slot is reserved
//        Booking booking = new Booking();
//        booking.setCustomerId(customerId);
//        booking.setProviderId(providerId);
//        booking.setServiceId(req.serviceId());
//        booking.setDateTime(req.dateTime());
//        booking.setLocation(req.location());
//        booking.setStatus(BookingStatus.ACCEPTED);
//        booking.setCreatedAt(LocalDateTime.now());
//
//        Booking saved = repo.save(booking);
//
//        slot.setStatus(AvailabilityStatus.BOOKED);
//        availabilityRepo.save(slot);
//
//        // Customer notification
//        notificationService.upsertBookingNotification(
//                saved.getCustomerId(),
//                saved.getId(),
//                "Provider Assigned. Your provider will service your booking."
//        );
//
//        // Provider notification
//        notificationService.upsertBookingNotification(
//                saved.getProviderId(),
//                saved.getId(),
//                "New Job Request: Booking #" + saved.getId()
//        );
//
//        return map(saved);
//    }
//
//    private Long pickProvider(Long serviceId, LocalDate date, LocalTime start, LocalTime end) {
//
//        List<Long> providerIds = providerServiceRepo.findProviderIdsBySubServiceId(serviceId);
//        if (providerIds.isEmpty()) return null;
//
//        for (Long pid : providerIds) {
//            boolean eligible = providerProfileRepo.findById(pid)
//                    .map(p -> p.isApproved() && p.isOnline())
//                    .orElse(false);
//            if (!eligible) continue;
//
//            // lock try: if slot is free, take this provider
//            if (availabilityRepo.lockAvailableSlot(pid, date, start, end).isPresent()) {
//                return pid;
//            }
//        }
//        return null;
//    }


    @Transactional
    public BookingResponse bookSlot(Long customerId, SlotBookingRequest req) {

        LocalDate date = req.dateTime().toLocalDate();
        LocalTime start = req.dateTime().toLocalTime();
        LocalTime end = start.plusHours(1);

        ProviderAvailability lockedSlot = null;
        Long providerId = req.providerId();

        // 1) If customer selected provider explicitly -> lock that slot
        if (providerId != null) {
            lockedSlot = availabilityRepo.lockAvailableSlot(providerId, date, start, end)
                    .orElseThrow(() -> new RuntimeException("Slot not available"));
        } else {
            // 2) Auto-pick provider: find eligible providers for service, then lock first available slot
            List<Long> providerIds = providerServiceRepo.findProviderIdsBySubServiceId(req.serviceId());
            if (providerIds.isEmpty()) throw new RuntimeException("No provider available for this service");

            for (Long pid : providerIds) {
                boolean eligible = providerProfileRepo.findById(pid)
                        .map(p -> p.isApproved() && p.isOnline())
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
                throw new RuntimeException("No provider available for this slot");
            }
        }

        // 3) Prevent double booking by checking booking table (extra safety)
        boolean overlap = repo.existsByProviderIdAndDateTimeBetweenAndStatusIn(
                providerId,
                req.dateTime(),
                req.dateTime().plusMinutes(59),
                List.of(BookingStatus.ACCEPTED, BookingStatus.COMPLETED)
        );
        if (overlap) throw new RuntimeException("Provider already booked for this slot");

        // 4) Create booking (provider assigned immediately)
        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setProviderId(providerId);
        booking.setServiceId(req.serviceId());
        booking.setDateTime(req.dateTime());
        booking.setLocation(req.location());
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setCreatedAt(LocalDateTime.now());

        SubService subService = subServiceRepo.findById(req.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        booking.setPrice(subService.getBasePrice());


        Booking saved = repo.save(booking);

        // 5) Mark slot as BOOKED
        lockedSlot.setStatus(AvailabilityStatus.BOOKED);
        availabilityRepo.save(lockedSlot);

        // 6) Notifications
        notificationService.upsertBookingNotification(
                saved.getCustomerId(),
                saved.getId(),
                "Provider Assigned. Your provider will service your booking."
        );

        notificationService.upsertBookingNotification(
                saved.getProviderId(),
                saved.getId(),
                "New Job Request: Booking #" + saved.getId()
        );

        return new BookingResponse(
                saved.getId(),
                saved.getCustomerId(),
                saved.getProviderId(),
                saved.getServiceId(),
                saved.getDateTime(),
                saved.getLocation(),
                saved.getStatus()
        );
    }


}

