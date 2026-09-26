package com.stocksense.service;

import com.stocksense.domain.*;
import com.stocksense.dto.ApiDtos.*;
import com.stocksense.exception.NotFoundException;
import com.stocksense.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional
public class CatalogService {
    private final CategoryRepository categories; private final ProductRepository products; private final WarehouseRepository warehouses;
    private final LocationRepository locations; private final SupplierRepository suppliers; private final CustomerRepository customers;
    public CatalogService(CategoryRepository categories,ProductRepository products,WarehouseRepository warehouses,LocationRepository locations,SupplierRepository suppliers,CustomerRepository customers) {
        this.categories=categories; this.products=products; this.warehouses=warehouses; this.locations=locations; this.suppliers=suppliers; this.customers=customers;
    }
    public List<CategoryView> categories() { return categories.findAll().stream().map(this::categoryView).toList(); }
    public CategoryView category(Long id) { return categoryView(category(id)); }
    public CategoryView createCategory(CategoryRequest r) { Category c=new Category(); set(c,r); return categoryView(categories.save(c)); }
    public CategoryView updateCategory(Long id,CategoryRequest r) { Category c=category(id); set(c,r); return categoryView(c); }
    public void deleteCategory(Long id) { categories.delete(category(id)); }
    public List<ProductView> products() { return products.findAll().stream().map(this::productView).toList(); }
    public ProductView product(Long id) { return productView(product(id)); }
    public ProductView createProduct(ProductRequest r) { Product p=new Product(); set(p,r); return productView(products.save(p)); }
    public ProductView updateProduct(Long id,ProductRequest r) { Product p=product(id); set(p,r); return productView(p); }
    public void deleteProduct(Long id) { products.delete(product(id)); }
    public List<WarehouseView> warehouses() { return warehouses.findAll().stream().map(this::warehouseView).toList(); }
    public WarehouseView warehouse(Long id) { return warehouseView(warehouses.findById(id).orElseThrow(()->missing("Warehouse",id))); }
    public WarehouseView createWarehouse(WarehouseRequest r) { Warehouse w=new Warehouse(); set(w,r); return warehouseView(warehouses.save(w)); }
    public WarehouseView updateWarehouse(Long id,WarehouseRequest r) { Warehouse w=warehouses.findById(id).orElseThrow(()->missing("Warehouse",id)); set(w,r); return warehouseView(w); }
    public void deleteWarehouse(Long id) { warehouses.delete(warehouses.findById(id).orElseThrow(()->missing("Warehouse",id))); }
    public List<LocationView> locations() { return locations.findAll().stream().map(this::locationView).toList(); }
    public LocationView location(Long id) { return locationView(locations.findById(id).orElseThrow(()->missing("Location",id))); }
    public LocationView createLocation(LocationRequest r) { Location l=new Location(); set(l,r); return locationView(locations.save(l)); }
    public LocationView updateLocation(Long id,LocationRequest r) { Location l=locations.findById(id).orElseThrow(()->missing("Location",id)); set(l,r); return locationView(l); }
    public void deleteLocation(Long id) { locations.delete(locations.findById(id).orElseThrow(()->missing("Location",id))); }
    public List<PartyView> suppliers() { return suppliers.findAll().stream().map(this::partyView).toList(); }
    public PartyView supplier(Long id) { return partyView(suppliers.findById(id).orElseThrow(()->missing("Supplier",id))); }
    public PartyView createSupplier(PartyRequest r) { Supplier p=new Supplier(); set(p,r); return partyView(suppliers.save(p)); }
    public PartyView updateSupplier(Long id,PartyRequest r) { Supplier p=suppliers.findById(id).orElseThrow(()->missing("Supplier",id)); set(p,r); return partyView(p); }
    public void deleteSupplier(Long id) { suppliers.delete(suppliers.findById(id).orElseThrow(()->missing("Supplier",id))); }
    public List<PartyView> customers() { return customers.findAll().stream().map(this::partyView).toList(); }
    public PartyView customer(Long id) { return partyView(customers.findById(id).orElseThrow(()->missing("Customer",id))); }
    public PartyView createCustomer(PartyRequest r) { Customer p=new Customer(); set(p,r); return partyView(customers.save(p)); }
    public PartyView updateCustomer(Long id,PartyRequest r) { Customer p=customers.findById(id).orElseThrow(()->missing("Customer",id)); set(p,r); return partyView(p); }
    public void deleteCustomer(Long id) { customers.delete(customers.findById(id).orElseThrow(()->missing("Customer",id))); }

    private void set(Category c,CategoryRequest r) { c.setName(r.name().trim()); c.setDescription(r.description()); }
    private void set(Product p,ProductRequest r) { p.setSku(r.sku().trim()); p.setName(r.name().trim()); p.setDescription(r.description()); p.setCategory(r.categoryId()==null?null:categories.findById(r.categoryId()).orElseThrow(()->missing("Category",r.categoryId()))); p.setUnitPrice(r.unitPrice()); p.setLowStockThreshold(r.lowStockThreshold()); if (r.active()!=null) p.setActive(r.active()); }
    private void set(Warehouse w,WarehouseRequest r) { w.setName(r.name().trim()); w.setAddress(r.address()); }
    private void set(Location l,LocationRequest r) { l.setName(r.name().trim()); l.setCode(r.code().trim()); l.setWarehouse(warehouses.findById(r.warehouseId()).orElseThrow(()->missing("Warehouse",r.warehouseId()))); }
    private void set(BusinessParty p,PartyRequest r) { p.setName(r.name().trim()); p.setEmail(r.email()); p.setPhone(r.phone()); p.setAddress(r.address()); }
    private Category category(Long id) { return categories.findById(id).orElseThrow(()->missing("Category",id)); }
    private Product product(Long id) { return products.findById(id).orElseThrow(()->missing("Product",id)); }
    private CategoryView categoryView(Category c) { return new CategoryView(c.getId(),c.getName(),c.getDescription()); }
    private ProductView productView(Product p) { return new ProductView(p.getId(),p.getSku(),p.getName(),p.getDescription(),p.getCategory()==null?null:p.getCategory().getId(),p.getCategory()==null?null:p.getCategory().getName(),p.getUnitPrice(),p.getLowStockThreshold(),p.isActive()); }
    private WarehouseView warehouseView(Warehouse w) { return new WarehouseView(w.getId(),w.getName(),w.getAddress()); }
    private LocationView locationView(Location l) { return new LocationView(l.getId(),l.getName(),l.getCode(),l.getWarehouse().getId(),l.getWarehouse().getName()); }
    private PartyView partyView(BusinessParty p) { return new PartyView(p.getId(),p.getName(),p.getEmail(),p.getPhone(),p.getAddress()); }
    private NotFoundException missing(String type,Long id) { return new NotFoundException(type+" "+id+" not found"); }
}
