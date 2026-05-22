package com.example.billing_backend.service;

import com.example.billing_backend.model.*;
import com.example.billing_backend.repository.CategoryRepository;
import com.example.billing_backend.repository.ProductRepository;
import com.example.billing_backend.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryService inventoryService;

    private void validateProductRules(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty())
            throw new RuntimeException("Rule Failed: Product name is mandatory.");
        if (product.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Rule Failed: Selling price must be greater than zero.");
        if (product.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0)
            throw new RuntimeException("Rule Failed: Purchase price cannot be negative.");

        if (product.getSellingPrice().compareTo(product.getPurchasePrice()) < 0) {
            throw new RuntimeException("Rule Failed: Selling price cannot be less than purchase price.");
        }

        if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Rule Failed: Cannot add an already expired product.");
        }

        List<Integer> validGst = Arrays.asList(0, 5, 12, 18, 28);
        if (product.getGstPercentage() != null && !validGst.contains(product.getGstPercentage().intValue()))
            throw new RuntimeException("Rule Failed: GST percentage must be valid (0, 5, 12, 18, 28).");
    }

    @Override
    public Product createProduct(Product product) {
        validateProductRules(product);

        if (productRepository.existsBySku(product.getSku()))
            throw new RuntimeException("Rule Failed: SKU must be unique.");
        if (product.getBarcode() != null && productRepository.existsByBarcode(product.getBarcode()))
            throw new RuntimeException("Rule Failed: Barcode must be unique.");

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Rule Failed: Selected Category does not exist."));
        product.setCategory(category);

        if (product.getSupplier() != null && product.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Rule Failed: Selected Supplier does not exist."));
            product.setSupplier(supplier);
        }

        product.setDeleted(false);
        if(product.getStatus() == null) product.setStatus(ProductStatus.ACTIVE);

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
        return productRepository.searchProducts(keyword);
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        validateProductRules(productDetails);

        Product existingProduct = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        if (!existingProduct.getSku().equals(productDetails.getSku()) && productRepository.existsBySku(productDetails.getSku()))
            throw new RuntimeException("Update Failed: SKU already exists!");
        if (productDetails.getBarcode() != null && !productDetails.getBarcode().equals(existingProduct.getBarcode()) && productRepository.existsByBarcode(productDetails.getBarcode()))
            throw new RuntimeException("Update Failed: Barcode already exists!");

        Category category = categoryRepository.findById(productDetails.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Update Failed: Selected Category does not exist."));

        existingProduct.setName(productDetails.getName());
        existingProduct.setSku(productDetails.getSku());
        existingProduct.setBarcode(productDetails.getBarcode());
        existingProduct.setPurchasePrice(productDetails.getPurchasePrice());
        existingProduct.setSellingPrice(productDetails.getSellingPrice());
        existingProduct.setGstPercentage(productDetails.getGstPercentage());
        existingProduct.setCategory(category);
        existingProduct.setBrand(productDetails.getBrand());
        existingProduct.setUnit(productDetails.getUnit());

        if (productDetails.getStatus() != null) {
            existingProduct.setStatus(productDetails.getStatus());
        }

        if (productDetails.getSupplier() != null && productDetails.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(productDetails.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Update Failed: Supplier does not exist."));
            existingProduct.setSupplier(supplier);
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        product.setDeleted(true);
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void processSupplierBill(MultipartFile file, Integer supplierId) {

        Supplier currentBillSupplier = null;
        if (supplierId != null) {
            currentBillSupplier = supplierRepository.findById(supplierId).orElse(null);
        }

        if (currentBillSupplier == null) {
            currentBillSupplier = Supplier.builder()
                    .companyName("Default Auto Supplier")
                    .gstin("AUTO-GST-" + System.currentTimeMillis())
                    .contactPerson("System Admin")
                    .mobile("9999999999")
                    .status(SupplierStatus.ACTIVE)
                    .build();
            currentBillSupplier = supplierRepository.save(currentBillSupplier);
        }

        Category defaultCategory = categoryRepository.findById(1L).orElse(null);
        if (defaultCategory == null) {
            defaultCategory = new Category();
            defaultCategory.setName("General Products");
            defaultCategory.setDescription("Auto-created for bill uploads");
            defaultCategory = categoryRepository.save(defaultCategory);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                for(int i=0; i<data.length; i++) data[i] = data[i].replaceAll("^\"|\"$", "").trim();

                if (data.length >= 7) {
                    String name = data[0];
                    String sku = data[1];
                    String brand = data[2];
                    String unit = data[3];
                    BigDecimal purchasePrice = new BigDecimal(data[4]);
                    BigDecimal sellingPrice = new BigDecimal(data[5]);

                    // 🔥 ENTERPRISE FIX: Inga thaan Double -> BigDecimal maathiyachu!
                    BigDecimal quantity = new BigDecimal(data[6]);

                    Product product = productRepository.findBySku(sku).orElse(null);

                    if (product == null) {
                        String dynamicBarcode = "BR" + System.currentTimeMillis() + (int)(Math.random() * 1000);

                        product = Product.builder()
                                .name(name)
                                .sku(sku)
                                .barcode(dynamicBarcode)
                                .brand(brand)
                                .unit(unit)
                                .purchasePrice(purchasePrice)
                                .sellingPrice(sellingPrice)
                                .gstPercentage(5.0)
                                .status(ProductStatus.ACTIVE)
                                .isDeleted(false)
                                .category(defaultCategory)
                                .supplier(currentBillSupplier)
                                .build();
                        product = productRepository.save(product);
                    } else {
                        product.setSupplier(currentBillSupplier);
                        productRepository.save(product);
                    }

                    // Ippo quantity BigDecimal-aaga irukkara kaaranathinaala error varadhu!
                    inventoryService.addStock(product.getId(), quantity);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process supplier bill file: " + e.getMessage());
        }
    }
}