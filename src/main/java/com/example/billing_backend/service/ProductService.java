package com.example.billing_backend.service;

import com.example.billing_backend.model.Product;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    List<Product> getProductsByCategory(Long categoryId);
    List<Product> searchProducts(String keyword);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);

    // PUDHU FEATURE: Bulk Upload from CSV
    void processSupplierBill(MultipartFile file);
}