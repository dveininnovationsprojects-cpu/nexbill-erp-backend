package com.example.billing_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(unique = true)
    private String barcode;

    // --- PRICING & COMPLIANCE ---
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private Double gstPercentage;
    private String hsnCode; // Indian GST-ku idhu romba mukkiyam!
    private Double discountPercentage; // Offer poduradhukku (Eg: 10% off)

    // --- ENTERPRISE ATTRIBUTES (For Supermarket & Zudio) ---
    private String brand; // E.g., "Aashirvaad", "Zudio", "Nike"

    private String unit; // Measurement Unit: "NOS" (Pieces), "KG", "GM", "LTR", "PACK"

    private String size; // For Clothes: "S", "M", "L", "XL". For Shoes: "8", "9"

    private String color; // "Red", "Blue", "Black"

    private LocalDate expiryDate; // Supermarket perishable items-ku

    // --- INVENTORY LINK ---
    // Note: Integer-ah irundha quantity ippo Double aagiduchu (For 1.5 KG etc.)
    private Double stockQuantity;
    private Double reorderLevel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;

    // Soft delete status
    private boolean isDeleted = false;
}