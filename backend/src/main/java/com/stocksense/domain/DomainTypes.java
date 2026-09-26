package com.stocksense.domain;

public final class DomainTypes {
    private DomainTypes() {}
    public enum RoleName { ADMIN, INVENTORY_MANAGER, WAREHOUSE_STAFF }
    public enum DocumentStatus { DRAFT, PICKED, PACKED, VALIDATED, CANCELLED }
    public enum LedgerType { RECEIPT, DELIVERY, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT_IN, ADJUSTMENT_OUT }
}
