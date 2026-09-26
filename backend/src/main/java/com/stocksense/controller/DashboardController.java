package com.stocksense.controller;

import com.stocksense.dto.ApiDtos.*;
import com.stocksense.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

@RestController @RequestMapping("/api/dashboard") @SecurityRequirement(name="bearerAuth")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service=service; }
    @GetMapping("/stats") public DashboardStats stats() { return service.stats(); }
    @GetMapping("/movements") public List<LedgerView> movements(@RequestParam(defaultValue="20") int size) { return service.movements(size); }
    @GetMapping("/category-stock") public List<CategoryStockView> categoryStock() { return service.categoryStock(); }
    @GetMapping("/low-stock") public List<StockView> lowStock() { return service.lowStock(); }
}
