package com.example.ServiceBooking.bookings.unifiedfiltering;



import com.example.ServiceBooking.auth.Role;
import com.example.ServiceBooking.auth.UserRepository;
import com.example.ServiceBooking.bookings.Booking;
import com.example.ServiceBooking.bookings.BookingRepository;
import com.example.ServiceBooking.bookings.dto.BookingFilterRequest;
import com.example.ServiceBooking.bookings.dto.BookingResponse;
import com.example.ServiceBooking.bookings.dto.BookingStatus;
import com.example.ServiceBooking.servicecatalog.ServiceCategory;
import com.example.ServiceBooking.servicecatalog.ServiceCategoryRepository;
import com.example.ServiceBooking.servicecatalog.SubServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingFilterService {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final ServiceCategoryRepository categoryRepo;
    private final SubServiceRepository subServiceRepo;

    public Page<BookingResponse> filterBookings(
            Long callerUserId,
            Role callerRole,
            BookingFilterRequest req,
            Pageable pageable
    ) {
        log.info("Booking filter request received");
        log.debug("Building dynamic booking filter specification (IDs not logged)");

        // 1) Resolve optional lists from names/category
        List<Long> customerIds;
        if (req.customerName() != null && !req.customerName().trim().isEmpty()) {
            customerIds = userRepo.findUserIdsByNameLike(req.customerName().trim());
            log.debug("Customer name filter resolved to userId list");
            if (customerIds.isEmpty()) {
                log.info("No customers matched customerName filter");
                return Page.empty(applySorting(req, pageable));
            }
        } else {
            customerIds = null;
        }

        List<Long> providerIds;
        if (req.providerName() != null && !req.providerName().trim().isEmpty()) {
            providerIds = userRepo.findUserIdsByNameLike(req.providerName().trim());
            log.debug("Provider name filter resolved to userId list");
            if (providerIds.isEmpty()) {
                log.info("No providers matched providerName filter");
                return Page.empty(applySorting(req, pageable));
            }
        } else {
            providerIds = null;
        }

        List<Long> serviceIds;
        if (req.serviceCategory() != null && !req.serviceCategory().trim().isEmpty()) {
            ServiceCategory cat = categoryRepo.findByNameIgnoreCase(req.serviceCategory().trim())
                    .orElseThrow(() -> {
                        log.warn("Service category not found");
                        return new RuntimeException("Service category not found");
                    });

            serviceIds = subServiceRepo.findSubServiceIdsByCategoryId(cat.getId());
            log.debug("Service category filter resolved to subServiceId list");
            if (serviceIds.isEmpty()) {
                log.info("No sub-services found for given service category");
                return Page.empty(applySorting(req, pageable));
            }
        } else {
            serviceIds = null;
        }

        // 2) Build base specification from request
//        Specification<Booking> spec = Specification.where((Specification<Booking>) null);
        Specification<Booking> spec = (root, q, cb) -> cb.conjunction();

        String cityFilter = resolveCityFilter(callerUserId, callerRole, req.city());
        if (cityFilter != null && !cityFilter.isBlank()) {
            String normalizedCity = cityFilter.trim();
            spec = spec.and((root, q, cb) -> cb.equal(root.get("city"), normalizedCity));
        }

        if (req.status() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), req.status()));
        }

        if (serviceIds != null) {
            spec = spec.and((root, q, cb) -> root.get("serviceId").in(serviceIds));
        }

        if (customerIds != null) {
            spec = spec.and((root, q, cb) -> root.get("customerId").in(customerIds));
        }

        if (providerIds != null) {
            spec = spec.and((root, q, cb) -> root.get("providerId").in(providerIds));
        }

        if (req.fromDate() != null) {
            LocalDateTime from = req.fromDate().atStartOfDay();
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("dateTime"), from));
        }

        if (req.toDate() != null) {
            LocalDateTime to = req.toDate().atTime(LocalTime.MAX);
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("dateTime"), to));
        }

        // 3) Apply role-based visibility constraint
        spec = spec.and(roleConstraint(callerUserId, callerRole));

        // 4) Apply sort mapping + pagination
        Pageable finalPageable = applySorting(req, pageable);

        Page<Booking> page = bookingRepo.findAll(spec, finalPageable);
        log.info("Booking filter executed successfully");

        return page.map(this::map);
    }

    private Specification<Booking> roleConstraint(Long callerUserId, Role role) {
        return (root, q, cb) -> {
            if (role == Role.ADMIN) {
                return cb.conjunction();
            }
            if (role == Role.CUSTOMER) {
                return cb.equal(root.get("customerId"), callerUserId);
            }
            if (role == Role.PROVIDER) {
                return cb.equal(root.get("providerId"), callerUserId);
            }
            log.warn("Unsupported role for booking filter");
            throw new RuntimeException("Not allowed");
        };
    }

    private String resolveCityFilter(Long callerUserId, Role role, String requestedCity) {
        if (role == Role.ADMIN) {
            return requestedCity;
        }

        String city = userRepo.findById(callerUserId)
                .map(u -> u.getCity())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (city == null || city.isBlank()) {
            throw new RuntimeException("User city not set");
        }
        return city;
    }

    private Pageable applySorting(BookingFilterRequest req, Pageable pageable) {
        String sortBy = req.sortBy();
        String sortOrder = req.sortOrder();

        // default sort
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if (sortBy != null && !sortBy.isBlank()) {
            String mapped;
            mapped = switch (sortBy) {
                case "bookingDate" -> "dateTime";
                case "amount" -> "price";
                case "status" -> "status";
                default -> {
                    log.warn("Invalid sortBy provided, using default sort");
                    yield null;
                }
            };

            if (mapped != null) {
                Sort.Direction dir = "ASC".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
                sort = Sort.by(dir, mapped);
            }
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
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
}
