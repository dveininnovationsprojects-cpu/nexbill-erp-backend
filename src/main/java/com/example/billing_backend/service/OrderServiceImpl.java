package com.example.billing_backend.service;

import com.example.billing_backend.dto.OrderItemRequestDto;
import com.example.billing_backend.dto.OrderRequestDto;
import com.example.billing_backend.model.*;
import com.example.billing_backend.repository.OrderRepository;
import com.example.billing_backend.repository.ProductRepository;
import com.example.billing_backend.repository.UserRepository;
import com.example.billing_backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    @Override
    @Transactional // Database fail aana rollback aaga
    public Order createOrder(OrderRequestDto request, String cashierEmail) {

        // 1. Fetch Logged-in Cashier
        User cashier = userRepository.findByEmail(cashierEmail)
                .orElseThrow(() -> new RuntimeException("Cashier not found!"));

        // 2. Fetch Customer & Validate Business Rules
        Customer customer = null;
        if (request.getCustomerId() != null) {
            // 🔥 FIX: Database-la irunthu first fetch pannanum!
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found!"));

            // Blacklist Validation
            if (customer.getStatus() == CustomerStatus.BLACKLISTED) {
                throw new RuntimeException("Cannot process bill. Customer is BLACKLISTED!");
            }

            // Credit Limit Validation
            if (request.getPaymentMode() == PaymentMode.CREDIT) {

                double currentOutstanding = customer.getOutstandingDebt() != null ? customer.getOutstandingDebt() : 0.0;
                double allowedLimit = customer.getCreditLimit() != null ? customer.getCreditLimit() : 0.0;

                double currentOrderEstimate = 0.0;
                for (OrderItemRequestDto itemDto : request.getItems()) {
                    Product product = productRepository.findByIdAndIsDeletedFalse(itemDto.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product ID " + itemDto.getProductId() + " not found!"));

                    double qty = itemDto.getQuantity().doubleValue();
                    double price = product.getSellingPrice().doubleValue();
                    double gst = product.getGstPercentage();

                    double base = price * qty;
                    double tax = (base * gst) / 100.0;
                    currentOrderEstimate += (base + tax);
                }

                if (request.getDiscountAmount() != null) {
                    currentOrderEstimate -= request.getDiscountAmount().doubleValue();
                }

                if ((currentOutstanding + currentOrderEstimate) > allowedLimit) {
                    throw new RuntimeException("Credit limit exceeded! Please clear your previous outstanding dues before using credit payment mode again.");
                }
            }
        } else if (request.getPaymentMode() == PaymentMode.CREDIT) {
            // Walk-in customer (no ID) cannot use credit
            throw new RuntimeException("Walk-in customers cannot use CREDIT payment mode!");
        }

        // 3. Initialize Order Entity Variables
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .invoiceNumber("INV-" + System.currentTimeMillis())
                .customer(customer)
                .cashier(cashier)
                .paymentMode(request.getPaymentMode())
                .externalTransactionRef(request.getExternalTransactionRef())
                .status(OrderStatus.COMPLETED)
                .build();

        // 4. Process Each Cart Item (Math Calculations & Stock Deduction)
        for (OrderItemRequestDto itemDto : request.getItems()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product ID " + itemDto.getProductId() + " not found!"));

            BigDecimal qty = itemDto.getQuantity();
            BigDecimal price = product.getSellingPrice();
            BigDecimal gstPercent = BigDecimal.valueOf(product.getGstPercentage());

            // Calculation Logic
            BigDecimal itemBaseTotal = price.multiply(qty);
            BigDecimal itemTax = itemBaseTotal.multiply(gstPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = itemBaseTotal.add(itemTax);

            subTotal = subTotal.add(itemBaseTotal);
            totalTax = totalTax.add(itemTax);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .unitPrice(price)
                    .gstPercentage(gstPercent)
                    .lineTotal(lineTotal)
                    .build();

            orderItems.add(orderItem);
            inventoryService.reduceStock(product.getId(), qty);
        }

        // 5. Finalize Grand Total
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = subTotal.add(totalTax).subtract(discount);

        order.setSubTotal(subTotal);
        order.setTaxAmount(totalTax);
        order.setDiscountAmount(discount);
        order.setGrandTotal(grandTotal);
        order.setItems(orderItems);

        // 6. Save Order to DB
        Order savedOrder = orderRepository.save(order);

        // 7. Trigger Notification for High Value Sales
        if (savedOrder.getGrandTotal().doubleValue() >= 10000.00) {
            notificationService.triggerHighValueSaleAlert(
                    cashier.getName(),
                    savedOrder.getInvoiceNumber(),
                    savedOrder.getGrandTotal().doubleValue()
            );
        }

        // 8. USER PROFILE SYNC (Cashier Stats Update)
        cashier.setTotalBillsGeneratedCount((cashier.getTotalBillsGeneratedCount() == null ? 0 : cashier.getTotalBillsGeneratedCount()) + 1);
        if (request.getPaymentMode() == PaymentMode.CASH) {
            Double currentCash = cashier.getTodaysCashCollected() == null ? 0.0 : cashier.getTodaysCashCollected();
            cashier.setTodaysCashCollected(currentCash + grandTotal.doubleValue());
        }
        userRepository.save(cashier);

        // 9. Customer Ledger Sync
        if (customer != null) {
            if (request.getPaymentMode() == PaymentMode.CREDIT) {
                customerService.updateCustomerLedger(customer.getId(), grandTotal.doubleValue(), 0.0);
            } else {
                customerService.updateCustomerLedger(customer.getId(), grandTotal.doubleValue(), grandTotal.doubleValue());
            }
        }

        return savedOrder;
    }
}