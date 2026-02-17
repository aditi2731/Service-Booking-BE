package com.example.ServiceBooking.admin;



import com.example.ServiceBooking.admin.dto.*;
import com.example.ServiceBooking.auth.Role;
import com.example.ServiceBooking.auth.Status;
import com.example.ServiceBooking.auth.User;
import com.example.ServiceBooking.auth.UserRepository;
import com.example.ServiceBooking.bookings.Booking;
import com.example.ServiceBooking.bookings.BookingRepository;
import com.example.ServiceBooking.bookings.dto.BookingStatus;
import com.example.ServiceBooking.notification.NotificationService;
import com.example.ServiceBooking.payment.PaymentRepository;
import com.example.ServiceBooking.payment.PaymentStatus;
import com.example.ServiceBooking.providermanagement.ProviderProfile;
import com.example.ServiceBooking.providermanagement.ProviderProfileRepository;
import com.example.ServiceBooking.servicecatalog.ServiceCategory;
import com.example.ServiceBooking.servicecatalog.ServiceCategoryRepository;
import com.example.ServiceBooking.servicecatalog.SubService;
import com.example.ServiceBooking.servicecatalog.SubServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminManagementService {

    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final ProviderProfileRepository providerProfileRepo;
    private final ServiceCategoryRepository categoryRepo;
    private final SubServiceRepository subServiceRepo;
    private final NotificationService notificationService;

    @Value("${platform.commission.rate:0.20}")
    private BigDecimal commissionRate;

    // ---------- DASHBOARD ----------

    public DashboardResponse dashboard() {
        log.trace("Entering dashboard method");
        log.info("Admin dashboard requested");
        log.debug("Computing dashboard metrics");

        long totalUsers = userRepo.count();
        long totalCustomers = userRepo.countByRole(Role.CUSTOMER);
        long totalProviders = userRepo.countByRole(Role.PROVIDER);

        long totalBookings = bookingRepo.count();
        long completedBookings = bookingRepo.countByStatus(BookingStatus.COMPLETED);

        long paidPayments = paymentRepo.countByStatus(PaymentStatus.PAID);

        // Gross revenue = sum of PAID payment amounts
        BigDecimal gross = paymentRepo.sumAmountByStatus(PaymentStatus.PAID);
        if (gross == null) {
            log.warn("No paid payments found, setting gross revenue to zero");
            gross = BigDecimal.ZERO;
        }

        BigDecimal platformCommission = gross.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal providerNet = gross.subtract(platformCommission).setScale(2, RoundingMode.HALF_UP);

        log.debug("Dashboard metrics computed successfully");
        return new DashboardResponse(
                totalUsers,
                totalCustomers,
                totalProviders,
                totalBookings,
                completedBookings,
                paidPayments,
                gross.setScale(2, RoundingMode.HALF_UP),
                platformCommission,
                providerNet
        );
    }

    // ---------- USER CONTROLS ----------

    public Page<User> viewCustomers(Pageable pageable) {
        log.trace("Entering viewCustomers method");
        log.debug("Fetching customers from repository");
        log.info("Admin view customers requested");
        Page<User> customers = userRepo.findByRole(Role.CUSTOMER, pageable);
        log.debug("Customers fetched successfully");
        return customers;
    }

    @Transactional
    public void suspendAccount(Long userId, boolean suspended) {
        log.trace("Entering suspendAccount method");
        log.debug("Processing account suspension request");
        log.info("Admin suspend/unsuspend account requested");

        User user = userRepo.findById(userId).orElseThrow(() -> {
            log.error("User not found");
            return new RuntimeException("User not found");
        });

        // Assumption: you have Status enum like ACTIVE/SUSPENDED
        user.setStatus(suspended ? Status.SUSPENDED : Status.ACTIVE);
        userRepo.save(user);

        notificationService.createSystemNotification(user.getId(),
                suspended ? "Your account has been suspended" : "Your account suspension has been removed");

        log.debug("Account suspension updated successfully");
        log.info("Account suspension updated");
    }

    @Transactional
    public void approveProvider(Long providerId, boolean approved) {
        log.trace("Entering approveProvider method");
        log.debug("Processing provider approval request");
        log.info("Admin provider approval requested");
        ProviderProfile profile = providerProfileRepo.findById(providerId)
                .orElseThrow(() -> {
                    log.error("Provider not found");
                    return new RuntimeException("Provider not found");
                });

        profile.setApproved(approved);
        providerProfileRepo.save(profile);

        notificationService.createSystemNotification(
                providerId,
                approved ? "Your profile has been approved" : "Your application was rejected"
        );

        log.debug("Provider approval updated successfully");
        log.info("Provider approval updated");
    }

    // ---------- SERVICE CONTROLS ----------

//    public Page<ServiceCategory> listCategories(Pageable pageable) {
//        log.trace("Entering listCategories method");
//        log.debug("Fetching categories from repository");
//        log.info("Admin list categories requested");
//        Page<ServiceCategory> categories = categoryRepo.findAll(pageable);
//        log.debug("Categories fetched successfully");
//        return categories;
//    }

//    @Transactional
//    public ServiceCategory createCategory(String name) {
//        log.trace("Entering createCategory method");
//        log.debug("Processing category creation");
//        log.info("Admin create category requested");
//        if (name == null || name.trim().isEmpty()) {
//            log.error("Category name is required but was empty");
//            throw new RuntimeException("Category name is required");
//        }
//
//        ServiceCategory c = new ServiceCategory();
//        c.setName(name.trim());
//        ServiceCategory saved = categoryRepo.save(c);
//        log.debug("Category created successfully");
//        return saved;
//    }

//    @Transactional
//    public ServiceCategory updateCategory(Long categoryId, String name) {
//        log.trace("Entering updateCategory method");
//        log.debug("Processing category update");
//        log.info("Admin update category requested");
//        if (name == null || name.trim().isEmpty()) {
//            log.error("Category name is required but was empty");
//            throw new RuntimeException("Category name is required");
//        }
//
//        ServiceCategory c = categoryRepo.findById(categoryId)
//                .orElseThrow(() -> {
//                    log.error("Category not found");
//                    return new RuntimeException("Category not found");
//                });
//
//        c.setName(name.trim());
//        ServiceCategory saved = categoryRepo.save(c);
//        log.debug("Category updated successfully");
//        return saved;
//    }

//    @Transactional
//    public void deleteCategory(Long categoryId) {
//        log.trace("Entering deleteCategory method");
//        log.debug("Processing category deletion");
//        log.info("Admin delete category requested");
//        if (!categoryRepo.existsById(categoryId)) {
//            log.error("Category not found");
//            throw new RuntimeException("Category not found");
//        }
//        categoryRepo.deleteById(categoryId);
//        log.debug("Category deleted successfully");
//    }

    /**
     * Pricing Rule: simplest version = update SubService price.
     */
    @Transactional
    public SubService updateSubServicePrice(Long subServiceId, BigDecimal price) {
        log.trace("Entering updateSubServicePrice method");
        log.debug("Processing subservice price update");
        log.info("Admin update pricing requested");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Price must be greater than zero");
            throw new RuntimeException("Price must be > 0");
        }

        SubService s = subServiceRepo.findById(subServiceId)
                .orElseThrow(() -> {
                    log.error("SubService not found");
                    return new RuntimeException("SubService not found");
                });

        s.setBasePrice(price); // adjust if your field name differs
        SubService saved = subServiceRepo.save(s);
        log.debug("SubService price updated successfully");
        return saved;
    }

    // ---------- BOOKING CONTROLS ----------

    public Page<Booking> viewAllBookings(Pageable pageable) {
        log.trace("Entering viewAllBookings method");
        log.debug("Fetching all bookings from repository");
        log.info("Admin view all bookings requested");
        Page<Booking> bookings = bookingRepo.findAllByOrderByCreatedAtDesc(pageable);
        log.debug("Bookings fetched successfully");
        return bookings;
    }

    @Transactional
    public void manualAssignProvider(Long bookingId, Long providerId) {
        log.trace("Entering manualAssignProvider method");
        log.debug("Processing manual provider assignment");
        log.info("Admin manual provider assignment requested");

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found");
                    return new RuntimeException("Booking not found");
                });

        ProviderProfile profile = providerProfileRepo.findById(providerId)
                .orElseThrow(() -> {
                    log.error("Provider not found");
                    return new RuntimeException("Provider not found");
                });

        if (!profile.isApproved()) {
            log.warn("Provider not approved, rejecting assignment");
            throw new RuntimeException("Provider not approved");
        }

        // minimal: allow assignment only if booking is PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Booking status is not PENDING, rejecting assignment");
            throw new RuntimeException("Only PENDING bookings can be assigned manually");
        }

        booking.setProviderId(providerId);
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepo.save(booking);

        // notify customer + provider (booking-based upsert)
        notificationService.upsertBookingNotification(
                booking.getCustomerId(),
                booking.getId(),
                "A provider has been assigned to your booking"
        );
        notificationService.upsertBookingNotification(
                providerId,
                booking.getId(),
                "You have been assigned Booking #" + booking.getId()
        );

        log.debug("Manual provider assignment completed successfully");
        log.info("Manual provider assignment completed");
    }

    // ---------- REPORTS ----------

    public BookingReportResponse bookingReport(LocalDateTime from, LocalDateTime to) {
        log.trace("Entering bookingReport method");
        log.debug("Processing booking report generation");
        log.info("Admin booking report requested");
        if (from == null || to == null) {
            log.error("from and to parameters are required");
            throw new RuntimeException("from and to are required");
        }
        if (to.isBefore(from)) {
            log.error("to parameter must be after from parameter");
            throw new RuntimeException("to must be after from");
        }

        long total = bookingRepo.countByCreatedAtBetween(from, to);
        long completed = bookingRepo.countByStatusAndCreatedAtBetween(BookingStatus.COMPLETED, from, to);
        long cancelled = bookingRepo.countByStatusAndCreatedAtBetween(BookingStatus.CANCELLED, from, to);

        log.debug("Booking report generated successfully");
        return new BookingReportResponse(total, completed, cancelled);
    }

    /**
     * Provider performance computed from COMPLETED bookings.
     * Since your simplified Payment entity no longer stores providerId,
     * we compute performance from bookings table.
     */
    public Page<ProviderPerformanceResponse> providerPerformance(Pageable pageable) {
        log.trace("Entering providerPerformance method");
        log.debug("Processing provider performance report generation");
        log.info("Admin provider performance report requested");
        Page<ProviderPerformanceResponse> result = providerProfileRepo.findAll(pageable).map(profile -> {
            Long providerId = profile.getUserId();

            long completed = bookingRepo.countByProviderIdAndStatus(providerId, BookingStatus.COMPLETED);

            BigDecimal gross = bookingRepo.sumPriceByProviderIdAndStatus(providerId, BookingStatus.COMPLETED);
            if (gross == null) {
                log.warn("No completed bookings found for provider, setting gross to zero");
                gross = BigDecimal.ZERO;
            }

            BigDecimal commission = gross.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal net = gross.subtract(commission).setScale(2, RoundingMode.HALF_UP);

            return new ProviderPerformanceResponse(providerId, completed,
                    gross.setScale(2, RoundingMode.HALF_UP), commission, net);
        });
        log.debug("Provider performance report generated successfully");
        return result;
    }
}

