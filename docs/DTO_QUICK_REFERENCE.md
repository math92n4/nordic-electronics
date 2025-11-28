# DTO Quick Reference Guide

A quick reference for developers working with DTOs in the Nordic Electronics application.

## DTO Locations

```
entity/dto/
├── address/      → AddressDTO, AddressRequestDTO
├── coupon/       → CouponRequestDTO, CouponResponseDTO
├── order/        → OrderRequestDTO, OrderResponseDTO, OrderProductRequestDTO, OrderProductResponseDTO
├── payment/      → PaymentRequestDTO, PaymentResponseDTO
├── product/      → ProductRequestDTO, ProductResponseDTO, ProductDTO
├── user/         → UserDTO, UserResponseDTO
└── warehouse/    → WarehouseRequestDTO, WarehouseResponseDTO
```

## Mapper Locations

```
entity/mapper/
├── CouponMapper.java
├── OrderMapper.java
├── PaymentMapper.java
├── ProductMapper.java
├── UserMapper.java
└── WarehouseMapper.java
```

## Common Patterns

### Converting Entity to DTO
```java
ProductResponseDTO dto = ProductMapper.toResponseDTO(product);
```

### Converting DTO to Entity
```java
Product product = ProductMapper.toEntity(productRequestDTO);
```

### Using Builder Pattern
```java
ProductResponseDTO dto = ProductResponseDTO.builder()
    .productId(uuid)
    .name("Product Name")
    .price(BigDecimal.valueOf(99.99))
    .build();
```

## Service Method Signatures

### WarehouseService
- `List<WarehouseResponseDTO> getAll()`
- `WarehouseResponseDTO getById(UUID id)`
- `WarehouseResponseDTO save(WarehouseRequestDTO dto)`
- `WarehouseResponseDTO update(UUID id, WarehouseRequestDTO dto)`

### ProductService
- `List<ProductResponseDTO> getAll()`
- `ProductResponseDTO getById(UUID id)`
- `ProductResponseDTO save(ProductRequestDTO dto)`
- `ProductResponseDTO update(UUID id, ProductRequestDTO dto)`

### CouponService
- `CouponResponseDTO getCouponById(UUID couponId)`
- `List<CouponResponseDTO> getAllActiveCoupons()`
- `CouponResponseDTO save(CouponRequestDTO dto)`

### OrderService
- `Order createOrder(OrderRequestDTO dto)`

## Controller Endpoints

### Warehouse
- `GET /api/postgresql/warehouses` → `List<WarehouseResponseDTO>`
- `POST /api/postgresql/warehouses` → `WarehouseRequestDTO` → `WarehouseResponseDTO`

### Product
- `GET /api/postgresql/products` → `List<ProductResponseDTO>`
- `POST /api/postgresql/products` → `ProductRequestDTO` → `ProductResponseDTO`

### Coupon
- `GET /api/postgresql/coupons/get-by-id` → `CouponResponseDTO`
- `POST /api/postgresql/coupons/create` → `CouponRequestDTO` → `CouponResponseDTO`

### Order
- `POST /api/postgresql/orders/create` → `OrderRequestDTO` → `OrderResponseDTO`

## Nested DTOs

### OrderRequestDTO contains:
- `AddressRequestDTO address`
- `List<OrderProductRequestDTO> orderProducts`

### OrderResponseDTO contains:
- `UserDTO user`
- `AddressDTO address`
- `List<OrderProductResponseDTO> orderProducts`

### OrderProductResponseDTO contains:
- `ProductDTO product`

## Common Field Types

- **UUIDs**: `java.util.UUID`
- **Money**: `java.math.BigDecimal`
- **Dates**: `java.time.LocalDate`
- **DateTimes**: `java.time.LocalDateTime`
- **Enums**: Converted to `String` in DTOs

## Error Handling

All mappers handle `null` inputs gracefully:
```java
if (entity == null) {
    return null;
}
```

Services throw appropriate exceptions:
- `EntityNotFoundException` - When entity not found
- `IllegalArgumentException` - When validation fails
- `RuntimeException` - For unexpected errors

## Tips

1. **Always use mappers** - Don't manually convert entities to DTOs
2. **Check nulls** - Mappers return null for null inputs
3. **Use builders** - Leverage `@Builder` for clean object creation
4. **Validate IDs** - Services validate that referenced entities exist
5. **Nested structures** - Be aware of nested DTOs in requests/responses

