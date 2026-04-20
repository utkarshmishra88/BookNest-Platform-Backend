package com.booknest.order.service.impl;

import com.booknest.order.client.CartClient;
import com.booknest.order.client.PaymentClient;
import com.booknest.order.client.WalletServiceClient;
import com.booknest.order.dto.OrderResponse;
import com.booknest.order.dto.PlaceOrderRequest;
import com.booknest.order.dto.VerifyPaymentRequest;
import com.booknest.order.entity.Order;
import com.booknest.order.entity.OrderItem;
import com.booknest.order.enums.OrderStatus;
import com.booknest.order.enums.PaymentMode;
import com.booknest.order.repository.OrderRepository;
import com.booknest.order.service.OrderConfirmationNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartClient cartClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private WalletServiceClient walletServiceClient;

    @Mock
    private OrderConfirmationNotifier orderConfirmationNotifier;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private PlaceOrderRequest placeOrderRequest;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId(1L);
        order.setUserId(1L);
        order.setTotalAmount(BigDecimal.valueOf(100.0));
        order.setPaymentMode(PaymentMode.COD);
        order.setStatus(OrderStatus.CONFIRMED);

        OrderItem item = new OrderItem();
        item.setOrderItemId(1L);
        item.setBookId(10L);
        item.setPrice(BigDecimal.valueOf(100.0));
        item.setQuantity(1);
        item.setLineTotal(BigDecimal.valueOf(100.0));
        order.addItem(item);

        placeOrderRequest = new PlaceOrderRequest();
        placeOrderRequest.setPaymentMode(PaymentMode.COD);
        placeOrderRequest.setShippingAddressId(1L);
    }

    @Test
    void testPlaceOrder_EmptyCart() {
        when(cartClient.getCartByUserId(1L)).thenReturn(Collections.emptyMap());

        assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, placeOrderRequest));
    }

    @Test
    void testPlaceOrder_COD() {
        Map<String, Object> cartResponse = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("bookId", 10);
        item1.put("price", 100.0);
        item1.put("quantity", 1);
        item1.put("title", "Test Book");
        items.add(item1);
        cartResponse.put("items", items);

        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.placeOrder(1L, placeOrderRequest);

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(cartClient, times(1)).clearCart(1L);
        verify(orderConfirmationNotifier, times(1)).notifyOrderConfirmed(any(Order.class));
    }

    @Test
    void testVerifyPayment_Success() {
        when(orderRepository.findByOrderIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));

        Map<String, Object> verifyResp = new HashMap<>();
        verifyResp.put("status", "SUCCESS");
        verifyResp.put("message", "Payment Successful");
        when(paymentClient.verifyPayment(anyMap())).thenReturn(verifyResp);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setOrderId(1L);

        OrderResponse response = orderService.verifyPayment(1L, req);

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(cartClient, times(1)).clearCart(1L);
    }

    @Test
    void testGetAllOrdersForAdmin() {
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(order));
        List<OrderResponse> orders = orderService.getAllOrdersForAdmin();
        assertEquals(1, orders.size());
    }

    @Test
    void testGetOrdersByUserId() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));
        List<OrderResponse> orders = orderService.getOrdersByUserId(1L);
        assertEquals(1, orders.size());
    }

    @Test
    void testGetOrderById() {
        when(orderRepository.findByOrderIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
        OrderResponse response = orderService.getOrderById(1L, 1L);
        assertEquals(1L, response.getOrderId());
    }

    @Test
    void testUpdateOrderStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, response.getStatus());
        verify(orderConfirmationNotifier, times(1)).notifyOrderStatusUpdated(order);
    }
}
