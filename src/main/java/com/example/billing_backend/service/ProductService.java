package com.example.billing_backend.service;

import com.example.billing_backend.model.Product;
import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    List<Product> getProductsByCategory(Long categoryId);
    List<Product> searchProducts(String keyword); // Cashier Search-ku
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id); // Idhu unmaiyila Soft Delete thaan pannum
}