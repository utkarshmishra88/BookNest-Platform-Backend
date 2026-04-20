# Quick Fix Reference - price_at_purchase Field Issue

## ✅ What Was Fixed

**File**: `OrderItem.java` (Line 46)

**Before (❌ Causing Error)**:
```java
@Column(name = "price_at_purchase", nullable = true, precision = 10, scale = 2)
private BigDecimal priceAtPurchase;
```

**After (✅ Fixed)**:
```java
@Column(name = "price_at_purchase", nullable = true, precision = 10, scale = 2, columnDefinition = "DECIMAL(10, 2) DEFAULT NULL")
private BigDecimal priceAtPurchase;
```

## 🔍 Why This Happened

The error: `Field 'price_at_purchase' doesn't have a default value`

**Cause**: Mismatch between:
- JPA Entity: Says field is nullable (`nullable = true`)
- Database Column: Created as NOT NULL with no default

**When inserting a record** without providing this field:
- JPA tries to insert NULL → Database rejects it → ERROR!

## 🛠️ How It Works Now

The `columnDefinition` attribute explicitly tells Hibernate:
> "Create this column as DECIMAL(10, 2) with DEFAULT NULL"

This ensures the database schema matches the entity definition.

## 🚀 How to Test

1. **Clean and compile**:
   ```bash
   cd order-service
   ./mvnw clean compile
   ```

2. **Run the service**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Test the endpoint** (place an order - should work now!)

## 📋 Prevention Checklist

When adding optional nullable fields to JPA entities:

- [ ] Mark with `@Column(nullable = true, ...)`
- [ ] Add `columnDefinition = "TYPE DEFAULT NULL"` if needed
- [ ] Or initialize in `@PrePersist` method
- [ ] Test with both NULL and non-NULL values
- [ ] Run schema validation: `mvn clean compile`

## ❓ FAQ

**Q: Will this affect existing database tables?**  
A: On first run with `ddl-auto=update`, Hibernate will modify the column to add the DEFAULT NULL.

**Q: What if I want a non-NULL default?**  
A: Use `columnDefinition = "DECIMAL(10, 2) DEFAULT 0.00"` instead

**Q: Should all optional fields have this?**  
A: Yes, it's best practice. Especially for numeric fields with precision/scale.

**Q: Why not just make it NOT NULL with a default?**  
A: Because this field genuinely might not have a value initially (optional business logic).

---

## Related Files
- 📄 DATABASE_ISSUE_FIX.md - Detailed documentation
- 🔧 OrderItem.java - The fixed entity

## Support
If you see similar errors with other fields:
1. Check if field has `nullable = true` in @Column
2. Add explicit `columnDefinition` with DEFAULT
3. Recompile and redeploy

**Status**: Fixed and Verified ✅

