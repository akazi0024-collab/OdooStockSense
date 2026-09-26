package com.stocksense.service;

import com.stocksense.domain.Stock;
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
    private final ProductRepository products; private final LocationRepository locations; private final SupplierRepository suppliers; private final CustomerRepository customers; private final StockRepository stocks; private final InventoryService inventory;
    public DashboardService(ProductRepository products,LocationRepository locations,SupplierRepository suppliers,CustomerRepository customers,StockRepository stocks,InventoryService inventory) { this.products=products; this.locations=locations; this.suppliers=suppliers; this.customers=customers; this.stocks=stocks; this.inventory=inventory; }
    @Transactional(readOnly=true) public DashboardStats stats() {
        List<Stock> all=stocks.findAll(); BigDecimal units=all.stream().map(Stock::getQuantity).reduce(BigDecimal.ZERO,BigDecimal::add);
        long low=all.stream().filter(s->s.getQuantity().compareTo(BigDecimal.valueOf(s.getProduct().getLowStockThreshold()))<=0).count();
        return new DashboardStats(products.count(),locations.count(),suppliers.count(),customers.count(),low,units);
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
