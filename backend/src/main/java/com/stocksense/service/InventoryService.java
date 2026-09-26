package com.stocksense.service;

import com.stocksense.domain.*;
import com.stocksense.domain.DomainTypes.DocumentStatus;
import com.stocksense.domain.DomainTypes.LedgerType;
import com.stocksense.dto.ApiDtos.*;
import com.stocksense.exception.BadRequestException;
import com.stocksense.exception.NotFoundException;
import com.stocksense.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {
    private final StockRepository stocks; private final ProductRepository products; private final LocationRepository locations;
    private final SupplierRepository suppliers; private final CustomerRepository customers; private final ReceiptRepository receipts;
    private final DeliveryRepository deliveries; private final TransferRepository transfers; private final AdjustmentRepository adjustments; private final LedgerRepository ledger;
    public InventoryService(StockRepository stocks,ProductRepository products,LocationRepository locations,SupplierRepository suppliers,CustomerRepository customers,ReceiptRepository receipts,DeliveryRepository deliveries,TransferRepository transfers,AdjustmentRepository adjustments,LedgerRepository ledger) {
        this.stocks=stocks; this.products=products; this.locations=locations; this.suppliers=suppliers; this.customers=customers; this.receipts=receipts; this.deliveries=deliveries; this.transfers=transfers; this.adjustments=adjustments; this.ledger=ledger;
    }

    @Transactional public DocumentView createReceipt(ReceiptRequest r) {
        Receipt doc=new Receipt(); doc.setReference(reference("RCV")); doc.setLocation(location(r.locationId()));
        if (r.supplierId()!=null) doc.setSupplier(suppliers.findById(r.supplierId()).orElseThrow(()->missing("Supplier",r.supplierId())));
        r.items().forEach(i->{ ReceiptItem item=new ReceiptItem(); item.setReceipt(doc); item.setProduct(product(i.productId())); item.setQuantity(i.quantity()); item.setUnitCost(i.unitCost()); doc.getItems().add(item); });
        return receiptView(receipts.save(doc));
    }
    @Transactional public DocumentView updateReceipt(Long id,ReceiptRequest r) {
        Receipt doc=receipts.lockById(id).orElseThrow(()->missing("Receipt",id)); requireStatus(doc.getStatus(),DocumentStatus.DRAFT);
        doc.setLocation(location(r.locationId())); doc.setSupplier(r.supplierId()==null?null:suppliers.findById(r.supplierId()).orElseThrow(()->missing("Supplier",r.supplierId()))); doc.getItems().clear();
        r.items().forEach(i->{ ReceiptItem item=new ReceiptItem(); item.setReceipt(doc); item.setProduct(product(i.productId())); item.setQuantity(i.quantity()); item.setUnitCost(i.unitCost()); doc.getItems().add(item); });
        return receiptView(doc);
    }
    @Transactional public void deleteReceipt(Long id) { Receipt doc=receipts.lockById(id).orElseThrow(()->missing("Receipt",id)); requireStatus(doc.getStatus(),DocumentStatus.DRAFT); receipts.delete(doc); }
    @Transactional(readOnly=true) public List<DocumentView> receipts() { return receipts.findAll().stream().map(this::receiptView).toList(); }
    @Transactional(readOnly=true) public DocumentView receipt(Long id) { return receiptView(receipts.findById(id).orElseThrow(()->missing("Receipt",id))); }
    @Transactional public DocumentView validateReceipt(Long id) {
        Receipt doc=receipts.lockById(id).orElseThrow(()->missing("Receipt",id)); requireStatus(doc.getStatus(),DocumentStatus.DRAFT);
        lockLocation(doc.getLocation().getId());
        for (ReceiptItem item:doc.getItems()) change(item.getProduct().getId(),doc.getLocation().getId(),item.getQuantity(),LedgerType.RECEIPT,doc.getReference());
        doc.setStatus(DocumentStatus.VALIDATED); return receiptView(doc);
    }

    @Transactional public DocumentView createDelivery(DeliveryRequest r) {
        Delivery doc=new Delivery(); doc.setReference(reference("SHP")); doc.setLocation(location(r.locationId()));
        if (r.customerId()!=null) doc.setCustomer(customers.findById(r.customerId()).orElseThrow(()->missing("Customer",r.customerId())));
        r.items().forEach(i->{ DeliveryItem item=new DeliveryItem(); item.setDelivery(doc); item.setProduct(product(i.productId())); item.setQuantity(i.quantity()); doc.getItems().add(item); });
        return deliveryView(deliveries.save(doc));
    }
    @Transactional public DocumentView updateDelivery(Long id,DeliveryRequest r) {
        Delivery doc=deliveries.lockById(id).orElseThrow(()->missing("Delivery",id)); requireStatus(doc.getStatus(),DocumentStatus.DRAFT);
        doc.setLocation(location(r.locationId())); doc.setCustomer(r.customerId()==null?null:customers.findById(r.customerId()).orElseThrow(()->missing("Customer",r.customerId()))); doc.getItems().clear();
        r.items().forEach(i->{ DeliveryItem item=new DeliveryItem(); item.setDelivery(doc); item.setProduct(product(i.productId())); item.setQuantity(i.quantity()); doc.getItems().add(item); });
        return deliveryView(doc);
    }
    @Transactional public void deleteDelivery(Long id) { Delivery doc=deliveries.lockById(id).orElseThrow(()->missing("Delivery",id)); requireStatus(doc.getStatus(),DocumentStatus.DRAFT); deliveries.delete(doc); }
    @Transactional(readOnly=true) public List<DocumentView> deliveries() { return deliveries.findAll().stream().map(this::deliveryView).toList(); }
    @Transactional(readOnly=true) public DocumentView delivery(Long id) { return deliveryView(deliveries.findById(id).orElseThrow(()->missing("Delivery",id))); }
    @Transactional public DocumentView pick(Long id) { Delivery d=deliveries.lockById(id).orElseThrow(()->missing("Delivery",id)); requireStatus(d.getStatus(),DocumentStatus.DRAFT); d.setStatus(DocumentStatus.PICKED); return deliveryView(d); }
    @Transactional public DocumentView pack(Long id) { Delivery d=deliveries.lockById(id).orElseThrow(()->missing("Delivery",id)); requireStatus(d.getStatus(),DocumentStatus.PICKED); d.setStatus(DocumentStatus.PACKED); return deliveryView(d); }
    @Transactional public DocumentView validateDelivery(Long id) {
        Delivery doc=deliveries.lockById(id).orElseThrow(()->missing("Delivery",id)); requireStatus(doc.getStatus(),DocumentStatus.PACKED);
        lockLocation(doc.getLocation().getId());
        for (DeliveryItem item:doc.getItems()) change(item.getProduct().getId(),doc.getLocation().getId(),item.getQuantity().negate(),LedgerType.DELIVERY,doc.getReference());
        doc.setStatus(DocumentStatus.VALIDATED); return deliveryView(doc);
    }

    @Transactional public TransferView createTransfer(TransferRequest r) {
        if (r.fromLocationId().equals(r.toLocationId())) throw new BadRequestException("Source and destination locations must differ");
        StockTransfer t=new StockTransfer(); t.setReference(reference("TRF")); t.setProduct(product(r.productId())); t.setFromLocation(location(r.fromLocationId())); t.setToLocation(location(r.toLocationId())); t.setQuantity(r.quantity());
        return transferView(transfers.save(t));
    }
    @Transactional public TransferView updateTransfer(Long id,TransferRequest r) {
        if (r.fromLocationId().equals(r.toLocationId())) throw new BadRequestException("Source and destination locations must differ");
        StockTransfer t=transfers.lockById(id).orElseThrow(()->missing("Transfer",id)); requireStatus(t.getStatus(),DocumentStatus.DRAFT);
        t.setProduct(product(r.productId())); t.setFromLocation(location(r.fromLocationId())); t.setToLocation(location(r.toLocationId())); t.setQuantity(r.quantity()); return transferView(t);
    }
    @Transactional public void deleteTransfer(Long id) { StockTransfer t=transfers.lockById(id).orElseThrow(()->missing("Transfer",id)); requireStatus(t.getStatus(),DocumentStatus.DRAFT); transfers.delete(t); }
    @Transactional(readOnly=true) public List<TransferView> transfers() { return transfers.findAll().stream().map(this::transferView).toList(); }
    @Transactional(readOnly=true) public TransferView transfer(Long id) { return transferView(transfers.findById(id).orElseThrow(()->missing("Transfer",id))); }
    @Transactional public TransferView validateTransfer(Long id) {
        StockTransfer t=transfers.lockById(id).orElseThrow(()->missing("Transfer",id)); requireStatus(t.getStatus(),DocumentStatus.DRAFT);
        lockLocations(t.getFromLocation().getId(),t.getToLocation().getId());
        change(t.getProduct().getId(),t.getFromLocation().getId(),t.getQuantity().negate(),LedgerType.TRANSFER_OUT,t.getReference());
        change(t.getProduct().getId(),t.getToLocation().getId(),t.getQuantity(),LedgerType.TRANSFER_IN,t.getReference());
        t.setStatus(DocumentStatus.VALIDATED); return transferView(t);
    }

    @Transactional public AdjustmentView createAdjustment(AdjustmentRequest r) {
        StockAdjustment a=new StockAdjustment(); a.setReference(reference("ADJ")); a.setProduct(product(r.productId())); a.setLocation(location(r.locationId()));
        BigDecimal system=stocks.findByProductIdAndLocationId(r.productId(),r.locationId()).map(Stock::getQuantity).orElse(BigDecimal.ZERO);
        a.setSystemQuantity(system); a.setPhysicalQuantity(r.physicalQuantity()); a.setQuantityDelta(r.physicalQuantity().subtract(system));
        a.setReason(r.reason().trim()); return adjustmentView(adjustments.save(a));
    }
    @Transactional public AdjustmentView updateAdjustment(Long id,AdjustmentRequest r) {
        StockAdjustment a=adjustments.lockById(id).orElseThrow(()->missing("Adjustment",id)); requireStatus(a.getStatus(),DocumentStatus.DRAFT);
        a.setProduct(product(r.productId())); a.setLocation(location(r.locationId())); a.setPhysicalQuantity(r.physicalQuantity()); a.setReason(r.reason().trim());
        BigDecimal system=stocks.findByProductIdAndLocationId(r.productId(),r.locationId()).map(Stock::getQuantity).orElse(BigDecimal.ZERO); a.setSystemQuantity(system); a.setQuantityDelta(r.physicalQuantity().subtract(system)); return adjustmentView(a);
    }
    @Transactional public void deleteAdjustment(Long id) { StockAdjustment a=adjustments.lockById(id).orElseThrow(()->missing("Adjustment",id)); requireStatus(a.getStatus(),DocumentStatus.DRAFT); adjustments.delete(a); }
    @Transactional(readOnly=true) public List<AdjustmentView> adjustments() { return adjustments.findAll().stream().map(this::adjustmentView).toList(); }
    @Transactional(readOnly=true) public AdjustmentView adjustment(Long id) { return adjustmentView(adjustments.findById(id).orElseThrow(()->missing("Adjustment",id))); }
    @Transactional public AdjustmentView validateAdjustment(Long id) {
        StockAdjustment a=adjustments.lockById(id).orElseThrow(()->missing("Adjustment",id)); requireStatus(a.getStatus(),DocumentStatus.DRAFT); lockLocation(a.getLocation().getId());
        BigDecimal system=stocks.findByProductIdAndLocationId(a.getProduct().getId(),a.getLocation().getId()).map(Stock::getQuantity).orElse(BigDecimal.ZERO);
        BigDecimal difference=a.getPhysicalQuantity().subtract(system);
        a.setSystemQuantity(system); a.setQuantityDelta(difference);
        LedgerType type=difference.signum()>=0?LedgerType.ADJUSTMENT_IN:LedgerType.ADJUSTMENT_OUT;
        change(a.getProduct().getId(),a.getLocation().getId(),difference,type,a.getReference()); a.setStatus(DocumentStatus.VALIDATED); return adjustmentView(a);
    }

    @Transactional(readOnly=true) public List<StockView> stock(Long locationId,Long productId) {
        List<Stock> result=locationId!=null?stocks.findByLocationId(locationId):productId!=null?stocks.findByProductId(productId):stocks.findAll();
        if (locationId!=null && productId!=null) result=result.stream().filter(s->s.getProduct().getId().equals(productId)).toList();
        return result.stream().map(this::stockView).toList();
    }
    @Transactional(readOnly=true) public List<StockView> stockForWarehouse(Long warehouseId) { return stocks.findByLocationWarehouseId(warehouseId).stream().map(this::stockView).toList(); }
    @Transactional(readOnly=true) public List<StockView> lowStock() { return stocks.findAll().stream().filter(s->s.getQuantity().signum()>0 && s.getQuantity().compareTo(BigDecimal.valueOf(s.getProduct().getLowStockThreshold()))<=0).map(this::stockView).toList(); }
    @Transactional(readOnly=true) public List<LedgerView> ledger(Long locationId,int size) {
        int bounded=Math.max(1,Math.min(size,200)); var page=PageRequest.of(0,bounded);
        List<StockLedger> entries=locationId==null?ledger.findAllByOrderByOccurredAtDesc(page):ledger.findByStockLocationIdOrderByOccurredAtDesc(locationId,page);
        return entries.stream().map(this::ledgerView).toList();
    }
    @Transactional(readOnly=true) public List<LedgerView> ledgerForProduct(Long productId,int size) {
        if (!products.existsById(productId)) throw missing("Product",productId);
        return ledger.findByStockProductIdOrderByOccurredAtDesc(productId,PageRequest.of(0,Math.max(1,Math.min(size,200)))).stream().map(this::ledgerView).toList();
    }

    private void change(Long productId,Long locationId,BigDecimal delta,LedgerType type,String ref) {
        Stock stock=stocks.lockForUpdate(productId,locationId).orElseGet(()->{
            Stock created=new Stock(); created.setProduct(product(productId)); created.setLocation(location(locationId)); created.setQuantity(BigDecimal.ZERO); return stocks.save(created);
        });
        BigDecimal next=stock.getQuantity().add(delta);
        if (next.signum()<0) throw new BadRequestException("Insufficient stock for product "+stock.getProduct().getSku()+" at location "+stock.getLocation().getName());
        stock.setQuantity(next);
        StockLedger entry=new StockLedger(); entry.setStock(stock); entry.setType(type); entry.setQuantityDelta(delta); entry.setBalanceAfter(next); entry.setReference(ref);
        Authentication actor=SecurityContextHolder.getContext().getAuthentication(); entry.setPerformedBy(actor==null?"system":actor.getName()); ledger.save(entry);
    }
    private void lockLocations(Long first,Long second) {
        List<Long> ids=new ArrayList<>(List.of(first,second)); ids.sort(Comparator.naturalOrder()); ids.forEach(this::lockLocation);
    }
    private void lockLocation(Long id) { locations.lockById(id).orElseThrow(()->missing("Location",id)); }
    private Location location(Long id) { return locations.findById(id).orElseThrow(()->missing("Location",id)); }
    private Product product(Long id) { return products.findById(id).orElseThrow(()->missing("Product",id)); }
    private void requireStatus(DocumentStatus actual,DocumentStatus expected) { if (actual!=expected) throw new BadRequestException("Document must be "+expected+"; current status is "+actual); }
    private String reference(String prefix) { return prefix+"-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(); }
    private NotFoundException missing(String type,Long id) { return new NotFoundException(type+" "+id+" not found"); }
    private DocumentView receiptView(Receipt d) { return new DocumentView(d.getId(),d.getReference(),d.getStatus().name(),d.getLocation().getId(),d.getLocation().getName(),d.getSupplier()==null?null:d.getSupplier().getId(),d.getSupplier()==null?null:d.getSupplier().getName(),d.getItems().stream().map(i->new ItemView(i.getProduct().getId(),i.getProduct().getSku(),i.getProduct().getName(),i.getQuantity(),i.getUnitCost())).toList(),d.getCreatedAt()); }
    private DocumentView deliveryView(Delivery d) { return new DocumentView(d.getId(),d.getReference(),d.getStatus().name(),d.getLocation().getId(),d.getLocation().getName(),d.getCustomer()==null?null:d.getCustomer().getId(),d.getCustomer()==null?null:d.getCustomer().getName(),d.getItems().stream().map(i->new ItemView(i.getProduct().getId(),i.getProduct().getSku(),i.getProduct().getName(),i.getQuantity(),null)).toList(),d.getCreatedAt()); }
    private TransferView transferView(StockTransfer t) { return new TransferView(t.getId(),t.getReference(),t.getStatus().name(),t.getProduct().getId(),t.getProduct().getName(),t.getFromLocation().getId(),t.getFromLocation().getName(),t.getToLocation().getId(),t.getToLocation().getName(),t.getQuantity(),t.getCreatedAt()); }
    private AdjustmentView adjustmentView(StockAdjustment a) { return new AdjustmentView(a.getId(),a.getReference(),a.getStatus().name(),a.getProduct().getId(),a.getProduct().getName(),a.getLocation().getId(),a.getSystemQuantity(),a.getPhysicalQuantity(),a.getQuantityDelta(),a.getReason(),a.getCreatedAt()); }
    private StockView stockView(Stock s) { return new StockView(s.getId(),s.getProduct().getId(),s.getProduct().getSku(),s.getProduct().getName(),s.getLocation().getId(),s.getLocation().getName(),s.getLocation().getWarehouse().getName(),s.getQuantity(),s.getProduct().getLowStockThreshold()); }
    private LedgerView ledgerView(StockLedger e) { return new LedgerView(e.getId(),e.getType().name(),e.getQuantityDelta(),e.getBalanceAfter(),e.getReference(),e.getStock().getProduct().getId(),e.getStock().getProduct().getName(),e.getStock().getLocation().getId(),e.getStock().getLocation().getName(),e.getPerformedBy(),e.getOccurredAt()); }
}
