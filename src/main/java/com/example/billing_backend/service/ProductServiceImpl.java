package com.example.billing_backend.service;

import com.example.billing_backend.model.Category;
import com.example.billing_backend.model.Product;
import com.example.billing_backend.repository.CategoryRepository;
import com.example.billing_backend.repository.ProductRepository;
import com.example.billing_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Strict Validation Method (Create & Update rendukum use pannalam)
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
        if (!validGst.contains(product.getGstPercentage()))
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
        product.setDeleted(false); // Default aaga active-la irukkum
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findByIsDeletedFalse(); // Deleted items varadhu
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

        // SOFT DELETE LOGIC: Data-va azhikkaama true nu aakivitu save panrom
        product.setDeleted(true);
        productRepository.save(product);
    }
}