package com.stocksense.controller;

import com.stocksense.dto.ApiDtos.*;
import com.stocksense.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

@RestController @RequestMapping("/api") @SecurityRequirement(name="bearerAuth")
public class InventoryController {
    private final InventoryService service;
    public InventoryController(InventoryService service) { this.service=service; }
    @GetMapping("/stock") public List<StockView> stock(@RequestParam(required=false) Long locationId,@RequestParam(required=false) Long productId) { return service.stock(locationId,productId); }
    @GetMapping("/stock/product/{productId}") public List<StockView> stockForProduct(@PathVariable Long productId) { return service.stock(null,productId); }
    @GetMapping("/stock/warehouse/{warehouseId}") public List<StockView> stockForWarehouse(@PathVariable Long warehouseId) { return service.stockForWarehouse(warehouseId); }
    @GetMapping("/stock/low") public List<StockView> lowStock() { return service.lowStock(); }
    @GetMapping("/ledger") public List<LedgerView> ledger(@RequestParam(required=false) Long locationId,@RequestParam(defaultValue="50") int size) { return service.ledger(locationId,size); }
    @GetMapping("/ledger/product/{productId}") public List<LedgerView> ledgerForProduct(@PathVariable Long productId,@RequestParam(defaultValue="50") int size) { return service.ledgerForProduct(productId,size); }

    @GetMapping("/receipts") public List<DocumentView> receipts() { return service.receipts(); }
    @GetMapping("/receipts/{id}") public DocumentView receipt(@PathVariable Long id) { return service.receipt(id); }
    @PostMapping("/receipts") public DocumentView createReceipt(@Valid @RequestBody ReceiptRequest r) { return service.createReceipt(r); }
    @PutMapping("/receipts/{id}") public DocumentView updateReceipt(@PathVariable Long id,@Valid @RequestBody ReceiptRequest r) { return service.updateReceipt(id,r); }
    @DeleteMapping("/receipts/{id}") public ResponseEntity<Void> deleteReceipt(@PathVariable Long id) { service.deleteReceipt(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/receipts/{id}/validate") public DocumentView validateReceipt(@PathVariable Long id) { return service.validateReceipt(id); }

    @GetMapping("/deliveries") public List<DocumentView> deliveries() { return service.deliveries(); }
    @GetMapping("/deliveries/{id}") public DocumentView delivery(@PathVariable Long id) { return service.delivery(id); }
    @PostMapping("/deliveries") public DocumentView createDelivery(@Valid @RequestBody DeliveryRequest r) { return service.createDelivery(r); }
    @PutMapping("/deliveries/{id}") public DocumentView updateDelivery(@PathVariable Long id,@Valid @RequestBody DeliveryRequest r) { return service.updateDelivery(id,r); }
    @DeleteMapping("/deliveries/{id}") public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) { service.deleteDelivery(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/deliveries/{id}/pick") public DocumentView pick(@PathVariable Long id) { return service.pick(id); }
    @PostMapping("/deliveries/{id}/pack") public DocumentView pack(@PathVariable Long id) { return service.pack(id); }
    @PostMapping("/deliveries/{id}/validate") public DocumentView validateDelivery(@PathVariable Long id) { return service.validateDelivery(id); }

    @GetMapping("/transfers") public List<TransferView> transfers() { return service.transfers(); }
    @GetMapping("/transfers/{id}") public TransferView transfer(@PathVariable Long id) { return service.transfer(id); }
    @PostMapping("/transfers") public TransferView createTransfer(@Valid @RequestBody TransferRequest r) { return service.createTransfer(r); }
    @PutMapping("/transfers/{id}") public TransferView updateTransfer(@PathVariable Long id,@Valid @RequestBody TransferRequest r) { return service.updateTransfer(id,r); }
    @DeleteMapping("/transfers/{id}") public ResponseEntity<Void> deleteTransfer(@PathVariable Long id) { service.deleteTransfer(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/transfers/{id}/validate") public TransferView validateTransfer(@PathVariable Long id) { return service.validateTransfer(id); }

    @GetMapping("/adjustments") public List<AdjustmentView> adjustments() { return service.adjustments(); }
    @GetMapping("/adjustments/{id}") public AdjustmentView adjustment(@PathVariable Long id) { return service.adjustment(id); }
    @PostMapping("/adjustments") public AdjustmentView createAdjustment(@Valid @RequestBody AdjustmentRequest r) { return service.createAdjustment(r); }
    @PutMapping("/adjustments/{id}") public AdjustmentView updateAdjustment(@PathVariable Long id,@Valid @RequestBody AdjustmentRequest r) { return service.updateAdjustment(id,r); }
    @DeleteMapping("/adjustments/{id}") public ResponseEntity<Void> deleteAdjustment(@PathVariable Long id) { service.deleteAdjustment(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/adjustments/{id}/validate") public AdjustmentView validateAdjustment(@PathVariable Long id) { return service.validateAdjustment(id); }
}
