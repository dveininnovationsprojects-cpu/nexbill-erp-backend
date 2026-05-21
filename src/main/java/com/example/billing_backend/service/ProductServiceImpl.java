package com.example.billing_backend.service;

import com.example.billing_backend.model.Category;
import com.example.billing_backend.model.Product;
import com.example.billing_backend.repository.CategoryRepository;
import com.example.billing_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryService inventoryService; // Stock Sync panna idhu thevai

    private void validateProductRules(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty())
            throw new RuntimeException("Rule Failed: Product name is mandatory.");

        if (product.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Rule Failed: Selling price must be greater than zero.");

        if (product.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0)
            throw new RuntimeException("Rule Failed: Purchase price cannot be negative.");

        if (product.getStockQuantity() < 0)
            throw new RuntimeException("Rule Failed: Stock quantity cannot be negative.");

        if (product.getReorderLevel() < 0)
            throw new RuntimeException("Rule Failed: Reorder level cannot be negative.");

        List<Integer> validGst = Arrays.asList(0, 5, 12, 18, 28);
        if (!validGst.contains(product.getGstPercentage().intValue()))
            throw new RuntimeException("Rule Failed: GST percentage must be valid (0, 5, 12, 18, 28).");
    }

    @Override
    public Product createProduct(Product product) {
        validateProductRules(product);

        if (productRepository.existsBySku(product.getSku()))
            throw new RuntimeException("Rule Failed: SKU must be unique.");

        if (productRepository.existsByBarcode(product.getBarcode()))
            throw new RuntimeException("Rule Failed: Barcode must be unique.");

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Rule Failed: Selected Category does not exist."));

        product.setCategory(category);
        product.setDeleted(false);
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findByIsDeletedFalse();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found or has been deleted!"));
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsDeletedFalse(categoryId);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword);
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        validateProductRules(productDetails);

        Product existingProduct = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        if (!existingProduct.getSku().equals(productDetails.getSku()) && productRepository.existsBySku(productDetails.getSku()))
            throw new RuntimeException("Update Failed: SKU already exists!");

        if (!existingProduct.getBarcode().equals(productDetails.getBarcode()) && productRepository.existsByBarcode(productDetails.getBarcode()))
            throw new RuntimeException("Update Failed: Barcode already exists!");

        Category category = categoryRepository.findById(productDetails.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Update Failed: Selected Category does not exist."));

        existingProduct.setName(productDetails.getName());
        existingProduct.setSku(productDetails.getSku());
        existingProduct.setBarcode(productDetails.getBarcode());
        existingProduct.setPurchasePrice(productDetails.getPurchasePrice());
        existingProduct.setSellingPrice(productDetails.getSellingPrice());
        existingProduct.setStockQuantity(productDetails.getStockQuantity());
        existingProduct.setGstPercentage(productDetails.getGstPercentage());
        existingProduct.setReorderLevel(productDetails.getReorderLevel());
        existingProduct.setCategory(category);

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        product.setDeleted(true);
        productRepository.save(product);
    }

    // PUDHU FEATURE: Bulk CSV Upload Logic
    @Override
    @Transactional
    public void processSupplierBill(MultipartFile file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            // Default Category (Upload panra products ellam idha default ah edukkum, illana Category ID pass pannanum)
            Category defaultCategory = categoryRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Please ensure at least one Category (ID: 1) exists in the database before uploading bills!"));

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Header row-ah skip pannidum
                }

                String[] data = line.split(",");

                if (data.length >= 7) {
                    String name = data[0].trim();
                    String sku = data[1].trim();
                    String brand = data[2].trim();
                    String unit = data[3].trim();
                    BigDecimal purchasePrice = new BigDecimal(data[4].trim());
                    BigDecimal sellingPrice = new BigDecimal(data[5].trim());
                    Double quantity = Double.parseDouble(data[6].trim());

                    Product product = productRepository.findBySku(sku).orElse(null);

                    if (product == null) {
                        // Product illana PUDHUSA create pandrom
                        product = Product.builder()
                                .name(name)
                                .sku(sku)
                                .brand(brand)
                                .unit(unit)
                                .purchasePrice(purchasePrice)
                                .sellingPrice(sellingPrice)
                                .stockQuantity(quantity)
                                .reorderLevel(10.0)
                                .gstPercentage(5.0)
                                .isDeleted(false)
                                .category(defaultCategory)
                                .build();
                        product = productRepository.save(product);
                    } else {
                        // Product already irundha Product Table-la stock update pandrom
                        product.setStockQuantity(product.getStockQuantity() + quantity);
                        productRepository.save(product);
                    }

                    // System-oda Core Inventory Table-laiyum stock sync pandrom
                    inventoryService.addStock(product.getId(), quantity);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process supplier bill file: " + e.getMessage());
        }
    }
}