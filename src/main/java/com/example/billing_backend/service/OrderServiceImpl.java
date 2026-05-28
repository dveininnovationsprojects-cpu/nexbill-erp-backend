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

        // 2. Fetch Customer (Optional)
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found!"));

            // AUTOMATION: Blacklist aana customer ku bill poda koodathu!
            if(customer.getStatus() == CustomerStatus.BLACKLISTED) {
                throw new RuntimeException("Cannot process bill. Customer is BLACKLISTED!");
            }
        } else if (request.getPaymentMode() == PaymentMode.CREDIT) {
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
                .externalTransactionRef(request.getExternalTransactionRef()) // 👈 INTA LINE-AH INJECT PANNUNGA!
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

        // ==========================================
        // 🚀 TRIGGER NOTIFICATION: HIGH TRANSACTION MONITORING RADAR FLAG
        // ==========================================
        if (savedOrder.getGrandTotal().doubleValue() >= 10000.00) {
            notificationService.triggerHighValueSaleAlert(
                    cashier.getName(),
                    savedOrder.getInvoiceNumber(),
                    savedOrder.getGrandTotal().doubleValue()
            );
        }
        // ==========================================

        // 7. USER PROFILE SYNC (Cashier Stats Update) ... remaining profile increment code runs ...

        // 7. USER PROFILE SYNC (Cashier Stats Update)
        cashier.setTotalBillsGeneratedCount((cashier.getTotalBillsGeneratedCount() == null ? 0 : cashier.getTotalBillsGeneratedCount()) + 1);
        if (request.getPaymentMode() == PaymentMode.CASH) {
            Double currentCash = cashier.getTodaysCashCollected() == null ? 0.0 : cashier.getTodaysCashCollected();
            cashier.setTodaysCashCollected(currentCash + grandTotal.doubleValue());
        }
        userRepository.save(cashier);

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