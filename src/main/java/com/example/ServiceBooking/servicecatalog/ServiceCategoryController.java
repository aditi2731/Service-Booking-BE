package com.example.ServiceBooking.servicecatalog;

import com.example.ServiceBooking.servicecatalog.dto.CategoryRequest;
import com.example.ServiceBooking.servicecatalog.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Service Category", description = "Endpoints for managing service categories in the catalog")
@RestController
@RequestMapping("/catalog/categories")
@RequiredArgsConstructor
public class ServiceCategoryController {

    private final ServiceCategoryService service;


    @Operation(summary = "Create a new service category - ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    // Create new category
    @PostMapping
    public void add(@Valid @RequestBody CategoryRequest request) {
        log.trace("Entering add method");
        log.debug("Processing add category request");
        log.info("Add category requested");
        try {
            service.add(request);
            log.debug("Category added successfully");
        } catch (Exception e) {
            log.error("Error adding category");
            throw e;
        }
    }

    @Operation(summary = "Update an existing service category - ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    // Update existing category
    @PutMapping("/{id}")
    public void update(@PathVariable Long id,
                       @Valid @RequestBody CategoryRequest request) {
        log.trace("Entering update method");
        log.debug("Processing update category request");
        log.info("Update category requested");
        try {
            service.update(id, request);
            log.debug("Category updated successfully");
        } catch (Exception e) {
            log.error("Error updating category");
            throw e;
        }
    }

    @Operation(summary = "Delete a service category - ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    // Delete category
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.trace("Entering delete method");
        log.debug("Processing delete category request");
        log.info("Delete category requested");
        try {
            service.delete(id);
            log.debug("Category deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting category");
            throw e;
        }
    }

    @Operation(summary = "List all service categories - PUBLIC")
    // List all categories
    @GetMapping
    public List<CategoryResponse> list() {
        log.trace("Entering list method");
        log.debug("Processing list categories request");
        log.info("List categories requested");
        try {
            List<CategoryResponse> categories = service.list();
            log.debug("Categories listed successfully");
            return categories;
        } catch (Exception e) {
            log.error("Error listing categories");
            throw e;
        }
    }
}

