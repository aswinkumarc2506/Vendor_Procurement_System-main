package com.procurement.procurement.controller.procurement;

import com.procurement.procurement.entity.procurement.PurchaseOrder;
import com.procurement.procurement.entity.procurement.PurchaseOrderItem;
import com.procurement.procurement.repository.procurement.PurchaseOrderRepository;
import com.procurement.procurement.repository.vendor.VendorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/procurement/purchase-order")
public class PurchaseOrderController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;

    public PurchaseOrderController(PurchaseOrderRepository purchaseOrderRepository, VendorRepository vendorRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.vendorRepository = vendorRepository;
    }

    // ===================== Create Purchase Order =====================
    @PostMapping("/create")
    public ResponseEntity<?> createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        // Validate Vendor existence
        if (purchaseOrder.getVendor() == null || purchaseOrder.getVendor().getId() == null) {
            return ResponseEntity.badRequest().body("Vendor information is required.");
        }

        Long vendorId = purchaseOrder.getVendor().getId();
        if (!vendorRepository.existsById(vendorId)) {
            return ResponseEntity.badRequest().body("Vendor not found with ID: " + vendorId);
        }

        // Generate PO Number if missing
        if (purchaseOrder.getPoNumber() == null || purchaseOrder.getPoNumber().isEmpty()) {
            purchaseOrder.setPoNumber("PO-" + System.currentTimeMillis());
        }

        purchaseOrder.setStatus("PENDING");
        purchaseOrder.setCreatedAt(java.time.LocalDateTime.now());
        purchaseOrder.setUpdatedAt(java.time.LocalDateTime.now());

        // Link items for bidirectional relationship
        if (purchaseOrder.getItems() != null) {
            for (PurchaseOrderItem item : purchaseOrder.getItems()) {
                item.setPurchaseOrder(purchaseOrder);
            }
        }

        // Link approvals if any
        if (purchaseOrder.getApprovals() != null) {
            for (com.procurement.procurement.entity.procurement.Approval approval : purchaseOrder.getApprovals()) {
                approval.setPurchaseOrder(purchaseOrder);
            }
        }

        PurchaseOrder savedPO = purchaseOrderRepository.save(purchaseOrder);
        return ResponseEntity.ok(savedPO);
    }

    // ===================== Get all Purchase Orders =====================
    @GetMapping("/all")
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    // ===================== Get Purchase Order by ID =====================
    @GetMapping("/{id}")
    public ResponseEntity<?> getPurchaseOrderById(@PathVariable Long id) {
        Optional<PurchaseOrder> poOpt = purchaseOrderRepository.findById(id);
        if (poOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Purchase Order not found");
        }
        return ResponseEntity.ok(poOpt.get());
    }

    // ===================== Update Purchase Order Status =====================
    @PatchMapping("/update-status/{id}")
    public ResponseEntity<String> updateStatus(@PathVariable Long id,
            @RequestParam String status) {
        Optional<PurchaseOrder> poOpt = purchaseOrderRepository.findById(id);
        if (poOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Purchase Order not found");
        }

        PurchaseOrder po = poOpt.get();
        po.setStatus(status);
        purchaseOrderRepository.save(po);

        return ResponseEntity.ok("Purchase Order status updated to " + status);
    }
}
