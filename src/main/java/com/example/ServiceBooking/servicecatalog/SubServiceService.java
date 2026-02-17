package com.example.ServiceBooking.servicecatalog;

import com.example.ServiceBooking.servicecatalog.dto.SubServiceRequest;
import com.example.ServiceBooking.servicecatalog.dto.SubServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubServiceService {

    private final SubServiceRepository subRepo;
    private final ServiceCategoryRepository categoryRepo;

    public void add(Long categoryId, SubServiceRequest request) {
        log.trace("Entering add method");
        log.debug("Processing add sub-service request");
        log.info("Adding new sub-service");

        ServiceCategory category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found");
                    return new RuntimeException("Category not found");
                });

        SubService sub = new SubService();
        sub.setName(request.name());
        sub.setDescription(request.description());
        sub.setBasePrice(request.basePrice());
        sub.setCategory(category);

        subRepo.save(sub);

        log.debug("Sub-service added successfully");
    }

    public void update(Long id, SubServiceRequest request) {
        log.trace("Entering update method");
        log.debug("Processing update sub-service request");
        log.info("Updating sub-service");

        SubService sub = subRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Sub-service not found");
                    return new RuntimeException("Sub-service not found");
                });

        sub.setName(request.name());
        sub.setDescription(request.description());
        sub.setBasePrice(request.basePrice());

        subRepo.save(sub);

        log.debug("Sub-service updated successfully");
    }

    public void delete(Long id) {
        log.trace("Entering delete method");
        log.debug("Processing delete sub-service request");
        log.info("Deleting sub-service");

        subRepo.deleteById(id);

        log.debug("Sub-service deleted successfully");
    }

    public List<SubServiceResponse> byCategory(Long categoryId) {
        log.trace("Entering byCategory method");
        log.debug("Fetching sub-services by category");
        log.info("Retrieving sub-services for category");

        List<SubServiceResponse> subServices = subRepo.findByCategory_Id(categoryId)
                .stream()
                .map(this::map)
                .toList();

        log.debug("Sub-services retrieved successfully");
        return subServices;
    }

    public List<SubServiceResponse> search(String keyword) {
        log.trace("Entering search method");
        log.debug("Processing sub-service search request");
        log.info("Searching sub-services");

        List<SubServiceResponse> results = subRepo.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::map)
                .toList();

        log.debug("Sub-service search completed successfully");
        return results;
    }

    private SubServiceResponse map(SubService s) {
        return new SubServiceResponse(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getBasePrice(),
                s.getCategory().getId()
        );
    }
}

