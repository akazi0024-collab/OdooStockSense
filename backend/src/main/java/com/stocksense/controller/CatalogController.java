package com.stocksense.controller;

import com.stocksense.dto.ApiDtos.*;
import com.stocksense.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

@RestController @RequestMapping("/api") @SecurityRequirement(name="bearerAuth")
public class CatalogController {
    private final CatalogService service;
    public CatalogController(CatalogService service) { this.service=service; }
    @GetMapping("/categories") public List<CategoryView> categories() { return service.categories(); }
    @GetMapping("/categories/{id}") public CategoryView category(@PathVariable Long id) { return service.category(id); }
    @PostMapping("/categories") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public CategoryView createCategory(@Valid @RequestBody CategoryRequest r) { return service.createCategory(r); }
    @PutMapping("/categories/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public CategoryView updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryRequest r) { return service.updateCategory(id,r); }
    @DeleteMapping("/categories/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> deleteCategory(@PathVariable Long id) { service.deleteCategory(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/products") public List<ProductView> products() { return service.products(); }
    @GetMapping("/products/{id}") public ProductView product(@PathVariable Long id) { return service.product(id); }
    @PostMapping("/products") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public ProductView createProduct(@Valid @RequestBody ProductRequest r) { return service.createProduct(r); }
    @PutMapping("/products/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public ProductView updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest r) { return service.updateProduct(id,r); }
    @DeleteMapping("/products/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> deleteProduct(@PathVariable Long id) { service.deleteProduct(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/warehouses") public List<WarehouseView> warehouses() { return service.warehouses(); }
    @GetMapping("/warehouses/{id}") public WarehouseView warehouse(@PathVariable Long id) { return service.warehouse(id); }
    @PostMapping("/warehouses") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public WarehouseView createWarehouse(@Valid @RequestBody WarehouseRequest r) { return service.createWarehouse(r); }
    @PutMapping("/warehouses/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public WarehouseView updateWarehouse(@PathVariable Long id,@Valid @RequestBody WarehouseRequest r) { return service.updateWarehouse(id,r); }
    @DeleteMapping("/warehouses/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) { service.deleteWarehouse(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/locations") public List<LocationView> locations() { return service.locations(); }
    @GetMapping("/locations/{id}") public LocationView location(@PathVariable Long id) { return service.location(id); }
    @PostMapping("/locations") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public LocationView createLocation(@Valid @RequestBody LocationRequest r) { return service.createLocation(r); }
    @PutMapping("/locations/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public LocationView updateLocation(@PathVariable Long id,@Valid @RequestBody LocationRequest r) { return service.updateLocation(id,r); }
    @DeleteMapping("/locations/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> deleteLocation(@PathVariable Long id) { service.deleteLocation(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/suppliers") public List<PartyView> suppliers() { return service.suppliers(); }
    @GetMapping("/suppliers/{id}") public PartyView supplier(@PathVariable Long id) { return service.supplier(id); }
    @PostMapping("/suppliers") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public PartyView createSupplier(@Valid @RequestBody PartyRequest r) { return service.createSupplier(r); }
    @PutMapping("/suppliers/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public PartyView updateSupplier(@PathVariable Long id,@Valid @RequestBody PartyRequest r) { return service.updateSupplier(id,r); }
    @DeleteMapping("/suppliers/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) { service.deleteSupplier(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/customers") public List<PartyView> customers() { return service.customers(); }
    @GetMapping("/customers/{id}") public PartyView customer(@PathVariable Long id) { return service.customer(id); }
    @PostMapping("/customers") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public PartyView createCustomer(@Valid @RequestBody PartyRequest r) { return service.createCustomer(r); }
    @PutMapping("/customers/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')") public PartyView updateCustomer(@PathVariable Long id,@Valid @RequestBody PartyRequest r) { return service.updateCustomer(id,r); }
    @DeleteMapping("/customers/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) { service.deleteCustomer(id); return ResponseEntity.noContent().build(); }
}
