package com.stocksense.service;

import com.stocksense.domain.Stock;
import com.stocksense.domain.Product;
import com.stocksense.domain.DomainTypes.DocumentStatus;
import com.stocksense.dto.ApiDtos.*;
import com.stocksense.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private final ProductRepository products; private final LocationRepository locations; private final SupplierRepository suppliers; private final CustomerRepository customers; private final StockRepository stocks; private final ReceiptRepository receipts; private final DeliveryRepository deliveries; private final TransferRepository transfers; private final InventoryService inventory;
    public DashboardService(ProductRepository products,LocationRepository locations,SupplierRepository suppliers,CustomerRepository customers,StockRepository stocks,ReceiptRepository receipts,DeliveryRepository deliveries,TransferRepository transfers,InventoryService inventory) { this.products=products; this.locations=locations; this.suppliers=suppliers; this.customers=customers; this.stocks=stocks; this.receipts=receipts; this.deliveries=deliveries; this.transfers=transfers; this.inventory=inventory; }
    @Transactional(readOnly=true) public DashboardStats stats() {
        List<Stock> all=stocks.findAll(); BigDecimal units=all.stream().map(Stock::getQuantity).reduce(BigDecimal.ZERO,BigDecimal::add);
        long low=all.stream().filter(s->s.getQuantity().compareTo(BigDecimal.valueOf(s.getProduct().getLowStockThreshold()))<=0).count();
        var totals=new java.util.HashMap<Long,BigDecimal>();
        all.forEach(s->totals.merge(s.getProduct().getId(),s.getQuantity(),BigDecimal::add));
        List<Product> catalog=products.findAll();
        long inStock=catalog.stream().filter(p->totals.getOrDefault(p.getId(),BigDecimal.ZERO).signum()>0).count();
        long lowItems=catalog.stream().filter(p->{ BigDecimal quantity=totals.getOrDefault(p.getId(),BigDecimal.ZERO); return quantity.signum()>0 && quantity.compareTo(BigDecimal.valueOf(p.getLowStockThreshold()))<=0; }).count();
        long outOfStock=catalog.stream().filter(p->totals.getOrDefault(p.getId(),BigDecimal.ZERO).signum()==0).count();
        long pendingReceipts=receipts.findAll().stream().filter(r->r.getStatus()!=DocumentStatus.VALIDATED && r.getStatus()!=DocumentStatus.CANCELLED).count();
        long pendingDeliveries=deliveries.findAll().stream().filter(d->d.getStatus()!=DocumentStatus.VALIDATED && d.getStatus()!=DocumentStatus.CANCELLED).count();
        long pendingTransfers=transfers.findAll().stream().filter(t->t.getStatus()!=DocumentStatus.VALIDATED && t.getStatus()!=DocumentStatus.CANCELLED).count();
        return new DashboardStats(products.count(),locations.count(),suppliers.count(),customers.count(),low,units,inStock,lowItems,outOfStock,pendingReceipts,pendingDeliveries,pendingTransfers);
    }
    @Transactional(readOnly=true) public List<LedgerView> movements(int size) { return inventory.ledger(null,size); }
    @Transactional(readOnly=true) public List<CategoryStockView> categoryStock() {
        Map<Long,CategoryStockView> totals=new LinkedHashMap<>();
        for (Stock stock:stocks.findAll()) {
            var category=stock.getProduct().getCategory(); if (category==null) continue;
            CategoryStockView prior=totals.get(category.getId());
            totals.put(category.getId(),new CategoryStockView(category.getId(),category.getName(),(prior==null?BigDecimal.ZERO:prior.quantity()).add(stock.getQuantity())));
        }
        return List.copyOf(totals.values());
    }
    @Transactional(readOnly=true) public List<StockView> lowStock() { return inventory.lowStock(); }
}
