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
        Category electronics=new Category(); electronics.setName("Electronics"); electronics.setDescription("Electronic devices and accessories"); categories.save(electronics);
        Category office=new Category(); office.setName("Office Supplies"); office.setDescription("Everyday workplace supplies"); categories.save(office);
        Warehouse warehouse=new Warehouse(); warehouse.setName("Central Warehouse"); warehouse.setAddress("100 Inventory Way"); warehouses.save(warehouse);
        Location shelf=new Location(); shelf.setName("Main Floor"); shelf.setCode("MAIN"); shelf.setWarehouse(warehouse); locations.save(shelf);
        Product laptop=new Product(); laptop.setSku("DEMO-LAPTOP-01"); laptop.setName("Business Laptop"); laptop.setCategory(electronics); laptop.setUnitPrice(new BigDecimal("899.00")); laptop.setLowStockThreshold(5); products.save(laptop);
        Product mouse=new Product(); mouse.setSku("DEMO-MOUSE-01"); mouse.setName("Wireless Mouse"); mouse.setCategory(electronics); mouse.setUnitPrice(new BigDecimal("29.99")); mouse.setLowStockThreshold(10); products.save(mouse);
        Product paper=new Product(); paper.setSku("DEMO-PAPER-01"); paper.setName("Copy Paper Ream"); paper.setCategory(office); paper.setUnitPrice(new BigDecimal("6.50")); paper.setLowStockThreshold(20); products.save(paper);
        addStock(laptop,shelf,"12.000"); addStock(mouse,shelf,"4.000"); addStock(paper,shelf,"32.000");
    }
    private void addStock(Product product,Location location,String quantity) {
        BigDecimal opening=new BigDecimal(quantity); Stock stock=new Stock(); stock.setProduct(product); stock.setLocation(location); stock.setQuantity(opening); stocks.save(stock);
        StockLedger event=new StockLedger(); event.setStock(stock); event.setType(LedgerType.ADJUSTMENT_IN); event.setQuantityDelta(opening); event.setBalanceAfter(opening); event.setReference("SEED-OPENING"); ledger.save(event);
    }
}
