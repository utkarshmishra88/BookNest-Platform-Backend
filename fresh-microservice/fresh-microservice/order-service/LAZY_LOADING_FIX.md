# Order Service - Lazy Loading Fix

## Issue Description

The application was experiencing the following errors:

```
Server error: 500 Cannot lazily initialize collection of role 
'com.booknest.order.entity.Order.items' with key '30' (no session)
```

This is a classic Hibernate lazy loading exception that occurs when trying to access a lazily-loaded collection outside of an active transaction context.

## Root Cause

In the `OrderServiceImpl.java`, the methods `getOrdersByUserId()` and `getOrderById()` were missing the `@Transactional` annotation. When these methods returned and the entity was serialized to JSON, the lazy-loaded `items` collection would fail to load because the Hibernate session had already been closed.

Additionally, the JSON serialization process happens after the transactional context ends, causing the lazy loading error.

## Solution

The fix involved two changes to `OrderServiceImpl.java`:

### 1. Added `@Transactional` to Query Methods

```java
@Override
@Transactional
public List<OrderResponse> getOrdersByUserId(Long userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(o -> toOrderResponse(o, o.getPaymentGatewayOrderId(), null, "OK"))
            .collect(Collectors.toList());
}

@Override
@Transactional
public OrderResponse getOrderById(Long userId, Long orderId) {
    Order order = orderRepository.findByOrderIdAndUserId(orderId, userId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    return toOrderResponse(order, order.getPaymentGatewayOrderId(), null, "OK");
}
```

### 2. Refactored `toOrderResponse()` to Access Items Within Transaction

The `toOrderResponse()` method now eagerly accesses the `order.getItems()` collection **before** building the response object. This ensures the items are fully loaded while the transaction is still active:

```java
private OrderResponse toOrderResponse(Order order, String razorpayOrderId,
                                      String razorpayKeyId, String message) {
    // Eagerly load items within transaction context
    List<OrderItemResponse> itemResponses = order.getItems().stream()
            .map(i -> OrderItemResponse.builder()
                    .orderItemId(i.getOrderItemId())
                    .bookId(i.getBookId())
                    .title(i.getTitle())
                    .author(i.getAuthor())
                    .imageUrl(i.getImageUrl())
                    .price(i.getPrice())
                    .quantity(i.getQuantity())
                    .lineTotal(i.getLineTotal())
                    .build())
            .collect(Collectors.toList());
    
    // Build response with already-loaded items
    return OrderResponse.builder()
            // ... other fields ...
            .items(itemResponses)
            .build();
}
```

## Why This Works

1. **@Transactional on Query Methods**: Ensures that a Hibernate session remains open while the method executes, including the mapping to DTOs.

2. **Eager Loading in Transaction**: By calling `order.getItems()` and converting items to DTOs **within** the `@Transactional` method, the collection is loaded while the session is active.

3. **DTO-Based Response**: The final response returns a DTO (`OrderResponse`) with pre-loaded data, avoiding serialization issues.

## Configuration Note

The application.yml already has the correct setting:
```yaml
spring:
  jpa:
    open-in-view: false
```

This is good for production as it prevents Hibernate sessions from remaining open during view rendering, which can cause performance issues. Our transactional approach ensures data is properly loaded before the session closes.

## Testing

After applying this fix:

1. The `/orders/{userId}` endpoint should return all orders without errors
2. The `/orders/{userId}/{orderId}` endpoint should return a single order with items without errors
3. The order items should be properly populated in the JSON response

## Build Verification

```
mvn clean package -DskipTests
```

Successfully built: `order-service-0.0.1-SNAPSHOT.jar`

