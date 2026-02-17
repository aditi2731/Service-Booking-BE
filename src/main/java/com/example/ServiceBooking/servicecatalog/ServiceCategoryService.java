package com.example.ServiceBooking.servicecatalog;

import com.example.ServiceBooking.servicecatalog.dto.CategoryRequest;
import com.example.ServiceBooking.servicecatalog.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCategoryService {

    private final ServiceCategoryRepository categoryRepo;

    public void add(CategoryRequest request) {
        log.trace("Entering add method");
        log.debug("Processing add category request");
        log.info("Adding new service category");

        ServiceCategory category = new ServiceCategory();
        category.setName(request.name());
        category.setDescription(request.description());
        categoryRepo.save(category);

        log.debug("Service category added successfully");
    }

    public void update(Long id, CategoryRequest request) {
        log.trace("Entering update method");
        log.debug("Processing update category request");
        log.info("Updating service category");

        ServiceCategory category = categoryRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Category not found");
                    return new RuntimeException("Category not found");
                });

        category.setName(request.name());
        category.setDescription(request.description());
        categoryRepo.save(category);

        log.debug("Service category updated successfully");
    }

    public void delete(Long id) {
        log.trace("Entering delete method");
        log.debug("Processing delete category request");
        log.info("Deleting service category");

        categoryRepo.deleteById(id);

        log.debug("Service category deleted successfully");
    }

    public List<CategoryResponse> list() {
        log.trace("Entering list method");
        log.debug("Fetching all service categories");
        log.info("Listing all service categories");

        List<CategoryResponse> categories = categoryRepo.findAll()
                .stream()
                .map(c -> new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getDescription()
                ))
                .toList();

        log.debug("Service categories retrieved successfully");
        return categories;
    }
}

