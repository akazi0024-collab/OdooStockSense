package com.stocksense.config;

import com.stocksense.domain.*;
import com.stocksense.domain.DomainTypes.RoleName;
import com.stocksense.domain.DomainTypes.LedgerType;
import com.stocksense.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
public class SeedData implements ApplicationRunner {
    private final UserRepository users; private final CategoryRepository categories; private final ProductRepository products; private final WarehouseRepository warehouses; private final LocationRepository locations; private final StockRepository stocks; private final LedgerRepository ledger; private final SupplierRepository suppliers; private final CustomerRepository customers; private final ReceiptRepository receipts; private final DeliveryRepository deliveries; private final TransferRepository transfers; private final AdjustmentRepository adjustments; private final PasswordEncoder encoder;
    public SeedData(UserRepository users,CategoryRepository categories,ProductRepository products,WarehouseRepository warehouses,LocationRepository locations,StockRepository stocks,LedgerRepository ledger,SupplierRepository suppliers,CustomerRepository customers,ReceiptRepository receipts,DeliveryRepository deliveries,TransferRepository transfers,AdjustmentRepository adjustments,PasswordEncoder encoder) { this.users=users; this.categories=categories; this.products=products; this.warehouses=warehouses; this.locations=locations; this.stocks=stocks; this.ledger=ledger; this.suppliers=suppliers; this.customers=customers; this.receipts=receipts; this.deliveries=deliveries; this.transfers=transfers; this.adjustments=adjustments; this.encoder=encoder; }
    @Override @Transactional public void run(ApplicationArguments args) {
        if (users.count()!=0 || categories.count()!=0 || products.count()!=0 || warehouses.count()!=0 || locations.count()!=0 || stocks.count()!=0 || suppliers.count()!=0 || customers.count()!=0 || receipts.count()!=0 || deliveries.count()!=0 || transfers.count()!=0 || adjustments.count()!=0 || ledger.count()!=0) return;
        AppUser admin=new AppUser(); admin.setName("StockSense Admin"); admin.setEmail("admin@stocksense.com"); admin.setPasswordHash(encoder.encode("StocksenseDev!2026")); admin.getRoles().add(RoleName.ADMIN); users.save(admin);
        Category raw=category("Raw Materials","Components and production materials");
        Category finished=category("Finished Goods","Ready-to-ship products");
        Category tools=category("Tools","Fasteners and maintenance supplies");
        Category safety=category("Safety Equipment","Protective equipment");
        Category electrical=category("Electrical","Electrical components and supplies");
        Warehouse main=warehouse("Main Warehouse","MAIN","100 Inventory Way");
        Warehouse production=warehouse("Production Warehouse","PROD","200 Factory Road");
        Warehouse finishedGoods=warehouse("Finished Goods Warehouse","FG","300 Dispatch Avenue");
        Location mainRack=location("Rack A","MAIN-A",main);
        Location mainFloor=location("Production Floor","PROD-FLOOR",production);
        Location finishedRack=location("Finished Rack","FG-A",finishedGoods);
        Supplier supplier=new Supplier(); supplier.setName("Northstar Industrial Supply"); supplier.setContactPerson("Morgan Lee"); supplier.setEmail("orders@northstar.example"); supplier.setPhone("+1 555 0100"); supplier.setAddress("18 Foundry Lane"); suppliers.save(supplier);
        Customer customer=new Customer(); customer.setName("Summit Office Group"); customer.setContactPerson("Taylor Reed"); customer.setEmail("purchasing@summit.example"); customer.setPhone("+1 555 0130"); customer.setAddress("90 Market Street"); customers.save(customer);
        Product steel=product("STL-ROD-001","Steel Rods",raw,"kg",30,"4.50");
        Product chairs=product("OFF-CHR-001","Office Chairs",finished,"unit",5,"145.00");
        Product panels=product("WD-PNL-001","Wood Panels",raw,"sheet",15,"22.00");
        Product bolts=product("TLS-BLT-001","Bolts",tools,"unit",50,"0.08");
        Product nuts=product("TLS-NUT-001","Nuts",tools,"unit",50,"0.04");
        Product helmets=product("SAFE-HELM-001","Safety Helmets",safety,"unit",8,"18.00");
        Product motors=product("EL-MTR-001","Electric Motors",electrical,"unit",4,"320.00");
        Product copper=product("EL-CU-001","Copper Wire",electrical,"m",25,"1.80");
        Product plastic=product("RAW-PLS-001","Plastic Sheets",raw,"sheet",10,"9.25");
        Product paint=product("RAW-PNT-001","Paint",raw,"L",12,"16.00");
        addStock(steel,mainRack,"100.000"); addStock(panels,mainRack,"75.000"); addStock(bolts,mainRack,"200.000"); addStock(copper,mainFloor,"40.000");
        addStock(chairs,finishedRack,"3.000"); addStock(helmets,finishedRack,"15.000"); addStock(motors,mainFloor,"6.000"); addStock(plastic,mainRack,"22.000"); addStock(paint,mainRack,"4.000");
        seedDrafts(supplier,customer,steel,chairs,mainRack,finishedRack,mainFloor);
    }
    private Category category(String name,String description) { Category value=new Category(); value.setName(name); value.setDescription(description); return categories.save(value); }
    private Warehouse warehouse(String name,String code,String address) { Warehouse value=new Warehouse(); value.setName(name); value.setCode(code); value.setAddress(address); return warehouses.save(value); }
    private Location location(String name,String code,Warehouse warehouse) { Location value=new Location(); value.setName(name); value.setCode(code); value.setWarehouse(warehouse); return locations.save(value); }
    private Product product(String sku,String name,Category category,String unit,int threshold,String price) { Product value=new Product(); value.setSku(sku); value.setName(name); value.setCategory(category); value.setUnitOfMeasure(unit); value.setLowStockThreshold(threshold); value.setUnitPrice(new BigDecimal(price)); return products.save(value); }
    private void seedDrafts(Supplier supplier,Customer customer,Product steel,Product chairs,Location main,Location finished,Location production) {
        Receipt receipt=new Receipt(); receipt.setReference("RCV-SEED-001"); receipt.setLocation(main); receipt.setSupplier(supplier);
        ReceiptItem receiptItem=new ReceiptItem(); receiptItem.setReceipt(receipt); receiptItem.setProduct(steel); receiptItem.setQuantity(new BigDecimal("20.000")); receiptItem.setUnitCost(new BigDecimal("4.50")); receipt.getItems().add(receiptItem); receipts.save(receipt);
        Delivery delivery=new Delivery(); delivery.setReference("SHP-SEED-001"); delivery.setLocation(finished); delivery.setCustomer(customer);
        DeliveryItem deliveryItem=new DeliveryItem(); deliveryItem.setDelivery(delivery); deliveryItem.setProduct(chairs); deliveryItem.setQuantity(new BigDecimal("1.000")); delivery.getItems().add(deliveryItem); deliveries.save(delivery);
        StockTransfer transfer=new StockTransfer(); transfer.setReference("TRF-SEED-001"); transfer.setProduct(steel); transfer.setFromLocation(main); transfer.setToLocation(production); transfer.setQuantity(new BigDecimal("10.000")); transfers.save(transfer);
    }
    private void addStock(Product product,Location location,String quantity) {
        BigDecimal opening=new BigDecimal(quantity); Stock stock=new Stock(); stock.setProduct(product); stock.setLocation(location); stock.setQuantity(opening); stocks.save(stock);
        StockLedger event=new StockLedger(); event.setStock(stock); event.setType(LedgerType.ADJUSTMENT_IN); event.setQuantityDelta(opening); event.setBalanceAfter(opening); event.setReference("SEED-OPENING"); event.setPerformedBy("system"); ledger.save(event);
    }
}
