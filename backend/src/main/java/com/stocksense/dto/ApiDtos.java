package com.stocksense.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}
    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank @Size(min=8,max=100) String password) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record ForgotRequest(@Email @NotBlank String email) {}
    public record VerifyOtpRequest(@Email @NotBlank String email, @NotBlank @Size(min=6,max=6) String otp) {}
    public record ResetRequest(@Email @NotBlank String email, @NotBlank String otp, @NotBlank @Size(min=8,max=100) String newPassword) {}
    public record AuthResponse(String token, Long id, String name, String email, List<String> roles) {}
    public record MessageResponse(String message, String devOtp) {}
    public record CategoryRequest(@NotBlank @Size(max=100) String name, @Size(max=500) String description) {}
    public record CategoryView(Long id, String name, String description) {}
    public record ProductRequest(@NotBlank @Size(max=80) String sku, @NotBlank @Size(max=180) String name, String description, Long categoryId, @NotNull @DecimalMin("0.00") BigDecimal unitPrice, @Min(0) int lowStockThreshold, Boolean active, @Size(max=24) String unitOfMeasure) {}
    public record ProductView(Long id, String sku, String name, String description, Long categoryId, String categoryName, BigDecimal unitPrice, int lowStockThreshold, boolean active, String unitOfMeasure) {}
    public record WarehouseRequest(@NotBlank @Size(max=120) String name, @Size(max=40) String code, String address) {}
    public record WarehouseView(Long id, String name, String code, String address) {}
    public record LocationRequest(@NotBlank @Size(max=80) String name, @NotBlank @Size(max=40) String code, @NotNull Long warehouseId) {}
    public record LocationView(Long id, String name, String code, Long warehouseId, String warehouseName) {}
    public record PartyRequest(@NotBlank @Size(max=160) String name, @Size(max=160) String contactPerson, @Email String email, String phone, String address) {}
    public record PartyView(Long id, String name, String contactPerson, String email, String phone, String address) {}
    public record ItemInput(@NotNull Long productId, @NotNull @DecimalMin(value="0.001") BigDecimal quantity, @DecimalMin("0.00") BigDecimal unitCost) {}
    public record ReceiptRequest(@NotNull Long locationId, Long supplierId, @NotEmpty List<@Valid ItemInput> items) {}
    public record DeliveryRequest(@NotNull Long locationId, Long customerId, @NotEmpty List<@Valid ItemInput> items) {}
    public record TransferRequest(@NotNull Long productId, @NotNull Long fromLocationId, @NotNull Long toLocationId, @NotNull @DecimalMin("0.001") BigDecimal quantity) {}
    public record AdjustmentRequest(@NotNull Long productId, @NotNull Long locationId, @NotNull @DecimalMin(value="0.000") BigDecimal physicalQuantity, @NotBlank String reason) {}
    public record ItemView(Long productId, String sku, String productName, BigDecimal quantity, BigDecimal unitCost) {}
    public record DocumentView(Long id, String reference, String status, Long locationId, String locationName, Long partyId, String partyName, List<ItemView> items, Instant createdAt) {}
    public record TransferView(Long id, String reference, String status, Long productId, String productName, Long fromLocationId, String fromLocationName, Long toLocationId, String toLocationName, BigDecimal quantity, Instant createdAt) {}
    public record AdjustmentView(Long id, String reference, String status, Long productId, String productName, Long locationId, BigDecimal systemQuantity, BigDecimal physicalQuantity, BigDecimal difference, String reason, Instant createdAt) {}
    public record StockView(Long id, Long productId, String sku, String productName, Long locationId, String locationName, String warehouseName, BigDecimal quantity, int lowStockThreshold) {}
    public record LedgerView(Long id, String type, BigDecimal quantityDelta, BigDecimal balanceAfter, String reference, Long productId, String productName, Long locationId, String locationName, String performedBy, Instant occurredAt) {}
    public record DashboardStats(long products, long locations, long suppliers, long customers, long lowStockLines, BigDecimal inventoryUnits, long productsInStock, long lowStockItems, long outOfStockItems, long pendingReceipts, long pendingDeliveries, long internalTransfers) {}
    public record CategoryStockView(Long categoryId, String categoryName, BigDecimal quantity) {}
}
