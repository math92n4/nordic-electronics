# DTO Implementation Documentation

## Overview

This document describes the Data Transfer Object (DTO) implementation for the Nordic Electronics application. DTOs provide a clean separation between the internal entity model and the external API contract, ensuring that clients only receive the data they need without exposing internal implementation details.

## Table of Contents

1. [DTO Structure](#dto-structure)
2. [DTO Organization](#dto-organization)
3. [Mappers](#mappers)
4. [Service Layer Updates](#service-layer-updates)
5. [Controller Layer Updates](#controller-layer-updates)
6. [API Examples](#api-examples)
7. [Migration Guide](#migration-guide)

---

## DTO Structure

### Package Organization

DTOs are organized by entity domain in the following structure:

```
src/main/java/com/example/nordicelectronics/entity/dto/
├── address/
│   ├── AddressDTO.java
│   └── AddressRequestDTO.java
├── coupon/
│   ├── CouponRequestDTO.java
│   └── CouponResponseDTO.java
├── order/
│   ├── OrderProductRequestDTO.java
│   ├── OrderProductResponseDTO.java
│   ├── OrderRequestDTO.java
│   └── OrderResponseDTO.java
├── payment/
│   ├── PaymentRequestDTO.java
│   └── PaymentResponseDTO.java
├── product/
│   ├── ProductDTO.java
│   ├── ProductRequestDTO.java
│   └── ProductResponseDTO.java
├── user/
│   ├── UserDTO.java
│   └── UserResponseDTO.java
└── warehouse/
    ├── WarehouseRequestDTO.java
    └── WarehouseResponseDTO.java
```

### DTO Annotations

All DTOs use Lombok annotations for clean, boilerplate-free code:

- `@Getter` - Generates getter methods
- `@Setter` - Generates setter methods
- `@NoArgsConstructor` - Generates no-args constructor
- `@AllArgsConstructor` - Generates all-args constructor
- `@Builder` - Enables builder pattern for object creation

---

## DTO Organization

### Address DTOs

#### AddressRequestDTO
Used for creating addresses within order requests.

**Fields:**
- `street` (String)
- `streetNumber` (String)
- `zip` (String)
- `city` (String)

#### AddressDTO
Used in order responses to represent shipping addresses.

**Fields:**
- `addressId` (UUID)
- `street` (String)
- `streetNumber` (String)
- `zip` (String)
- `city` (String)

### Coupon DTOs

#### CouponRequestDTO
Used for creating and updating coupons.

**Fields:**
- `code` (String)
- `discountType` (DiscountType enum: `percentage` or `fixed_amount`)
- `discountValue` (BigDecimal)
- `minimumOrderValue` (BigDecimal)
- `expiryDate` (LocalDate)
- `usageLimit` (Integer)
- `isActive` (Boolean)

#### CouponResponseDTO
Response DTO for coupon operations.

**Fields:**
- All fields from `CouponRequestDTO`
- `couponId` (UUID)
- `timesUsed` (Integer)

### Order DTOs

#### OrderRequestDTO
Used for creating new orders.

**Fields:**
- `userId` (UUID)
- `address` (AddressRequestDTO) - Nested address object
- `orderProducts` (List<OrderProductRequestDTO>) - List of products in the order
- `couponCode` (String) - Optional coupon code

#### OrderResponseDTO
Response DTO for order operations.

**Fields:**
- `orderId` (UUID)
- `user` (UserDTO) - Nested user information
- `address` (AddressDTO) - Shipping address
- `status` (String) - Order status enum as string
- `orderProducts` (List<OrderProductResponseDTO>) - List of order items

#### OrderProductRequestDTO
Represents a product in an order request.

**Fields:**
- `productId` (UUID)
- `quantity` (Integer)
- `warehouseId` (UUID)

#### OrderProductResponseDTO
Represents a product in an order response.

**Fields:**
- `product` (ProductDTO) - Nested product information
- `quantity` (Integer)
- `unitPrice` (BigDecimal)
- `totalPrice` (BigDecimal)

### Product DTOs

#### ProductRequestDTO
Used for creating and updating products.

**Fields:**
- `sku` (String)
- `name` (String)
- `description` (String)
- `price` (BigDecimal)
- `weight` (BigDecimal)
- `warrantyId` (UUID)
- `brandId` (UUID)
- `categoryIds` (List<UUID>)

#### ProductResponseDTO
Response DTO for product operations.

**Fields:**
- All fields from `ProductRequestDTO`
- `productId` (UUID)
- `reviewIds` (List<UUID>)

#### ProductDTO
Simplified product representation used in nested contexts (e.g., order products).

**Fields:**
- `productId` (UUID)
- `name` (String)
- `description` (String)
- `price` (BigDecimal)
- `weight` (BigDecimal)
- `warrantyId` (UUID)
- `brandId` (UUID)

### User DTOs

#### UserDTO
Simplified user representation used in nested contexts (e.g., order responses).

**Fields:**
- `userId` (UUID)
- `firstName` (String)
- `lastName` (String)
- `email` (String)

#### UserResponseDTO
Full user response DTO.

**Fields:**
- All fields from `UserDTO`
- `phoneNumber` (String)
- `dateOfBirth` (LocalDate)
- `isAdmin` (Boolean)

### Warehouse DTOs

#### WarehouseRequestDTO
Used for creating and updating warehouses.

**Fields:**
- `name` (String)
- `phoneNumber` (String)
- `addressId` (UUID)

#### WarehouseResponseDTO
Response DTO for warehouse operations.

**Fields:**
- All fields from `WarehouseRequestDTO`
- `warehouseId` (UUID)

---

## Mappers

Mappers provide conversion between entity objects and DTOs. All mappers are located in `com.example.nordicelectronics.entity.mapper` package.

### WarehouseMapper

**Methods:**
- `toResponseDTO(Warehouse warehouse)` - Converts Warehouse entity to WarehouseResponseDTO
- `toEntity(WarehouseRequestDTO dto)` - Converts WarehouseRequestDTO to Warehouse entity

### ProductMapper

**Methods:**
- `toResponseDTO(Product product)` - Converts Product entity to ProductResponseDTO
  - Automatically extracts categoryIds and reviewIds from relationships
- `toEntity(ProductRequestDTO dto)` - Converts ProductRequestDTO to Product entity

### CouponMapper

**Methods:**
- `toResponseDTO(Coupon coupon)` - Converts Coupon entity to CouponResponseDTO
- `toEntity(CouponRequestDTO dto)` - Converts CouponRequestDTO to Coupon entity
  - Sets `timesUsed` to 0 for new coupons

### UserMapper

**Methods:**
- `toResponseDTO(User user)` - Converts User entity to UserResponseDTO
- `toDTO(User user)` - Converts User entity to simplified UserDTO

### OrderMapper

**Methods:**
- `toResponseDTO(Order order)` - Converts Order entity to OrderResponseDTO
  - Maps nested User to UserDTO
  - Maps nested Address to AddressDTO
  - Maps OrderStatus enum to String
  - Maps OrderProducts to OrderProductResponseDTO list
- `toOrderProductResponseDTO(OrderProduct orderProduct)` - Private helper method
  - Converts OrderProduct to OrderProductResponseDTO
  - Maps nested Product to ProductDTO

### PaymentMapper

**Methods:**
- `toResponseDTO(Payment payment)` - Converts Payment entity to PaymentResponseDTO
  - Converts enum values to strings for API compatibility

---

## Service Layer Updates

### WarehouseService

**Updated Methods:**
- `getAll()` - Returns `List<WarehouseResponseDTO>` instead of `List<Warehouse>`
- `getById(UUID id)` - Returns `WarehouseResponseDTO` instead of `Warehouse`
- `save(WarehouseRequestDTO dto)` - Accepts DTO, creates entity, saves, and returns DTO
- `update(UUID id, WarehouseRequestDTO dto)` - Accepts DTO, updates entity, and returns DTO

**Key Changes:**
- Service now handles Address lookup by addressId
- All methods use WarehouseMapper for conversions

### ProductService

**Updated Methods:**
- `getAll()` - Returns `List<ProductResponseDTO>` instead of `List<Product>`
- `getById(UUID id)` - Returns `ProductResponseDTO` instead of `Product`
- `save(ProductRequestDTO dto)` - Accepts DTO, handles brand/warranty/category lookups, saves, and returns DTO
- `update(UUID id, ProductRequestDTO dto)` - Accepts DTO, updates entity, and returns DTO

**Key Changes:**
- Service handles categoryIds list conversion to Category entities
- Automatically extracts reviewIds from Product relationships

### CouponService

**Updated Methods:**
- `getCouponById(UUID couponId)` - Returns `CouponResponseDTO` instead of `Coupon`
- `getAllCouponsByOrderId(UUID orderId)` - Returns `List<CouponResponseDTO>`
- `getAllActiveCoupons()` - Returns `List<CouponResponseDTO>`
- `getAllInactiveCoupons()` - Returns `List<CouponResponseDTO>`
- `save(CouponRequestDTO dto)` - Accepts DTO, creates entity, saves, and returns DTO

**Key Changes:**
- All methods use CouponMapper for conversions
- Service now throws proper exceptions when coupon not found

### OrderService

**Updated Methods:**
- `createOrder(OrderRequestDTO dto)` - Accepts OrderRequestDTO instead of Order entity

**Key Changes:**
- Creates Address entity from AddressRequestDTO if provided
- Falls back to user's first address if no address provided
- Looks up Coupon by code if couponCode provided
- Creates OrderProduct entities from OrderProductRequestDTO list
- Calculates subtotal from order products
- All entity creation and relationship management handled internally

---

## Controller Layer Updates

### WarehouseController

All endpoints now use DTOs:

- `GET /api/postgresql/warehouses` - Returns `List<WarehouseResponseDTO>`
- `GET /api/postgresql/warehouses/{id}` - Returns `WarehouseResponseDTO`
- `POST /api/postgresql/warehouses` - Accepts `WarehouseRequestDTO`, returns `WarehouseResponseDTO`
- `PUT /api/postgresql/warehouses/{id}` - Accepts `WarehouseRequestDTO`, returns `WarehouseResponseDTO`
- `DELETE /api/postgresql/warehouses/{id}` - Returns `Void`

### ProductController

All endpoints now use DTOs:

- `GET /api/postgresql/products` - Returns `List<ProductResponseDTO>`
- `GET /api/postgresql/products/{id}` - Returns `ProductResponseDTO`
- `POST /api/postgresql/products` - Accepts `ProductRequestDTO`, returns `ProductResponseDTO`
- `PUT /api/postgresql/products/{id}` - Accepts `ProductRequestDTO`, returns `ProductResponseDTO`
- `DELETE /api/postgresql/products/{id}` - Returns `Void`

### CouponController

All endpoints now use DTOs:

- `GET /api/postgresql/coupons/get-by-order-id` - Returns `List<CouponResponseDTO>`
- `GET /api/postgresql/coupons/get-by-id` - Returns `CouponResponseDTO`
- `GET /api/postgresql/coupons/get-active` - Returns `List<CouponResponseDTO>`
- `GET /api/postgresql/coupons/get-inactive` - Returns `List<CouponResponseDTO>`
- `POST /api/postgresql/coupons/create` - Accepts `CouponRequestDTO`, returns `CouponResponseDTO`

### OrderController

Updated endpoints:

- `POST /api/postgresql/orders/create` - Accepts `OrderRequestDTO`, returns `OrderResponseDTO`
- `GET /api/postgresql/orders/by-ids` - Returns `List<OrderResponseDTO>`

### PaymentController

Already uses DTOs (no changes needed):

- `GET /api/postgresql/payments/by-order` - Returns `PaymentResponseDTO`
- `POST /api/postgresql/payments/create` - Accepts `PaymentRequestDTO`, returns `PaymentResponseDTO`

---

## API Examples

### Create Warehouse

**Request:**
```http
POST /api/postgresql/warehouses
Content-Type: application/json

{
  "name": "Main Warehouse",
  "phoneNumber": "+45 12 34 56 78",
  "addressId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "warehouseId": "660e8400-e29b-41d4-a716-446655440000",
  "name": "Main Warehouse",
  "phoneNumber": "+45 12 34 56 78",
  "addressId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Create Product

**Request:**
```http
POST /api/postgresql/products
Content-Type: application/json

{
  "sku": "PROD-001",
  "name": "Wireless Headphones",
  "description": "High-quality wireless headphones with noise cancellation",
  "price": 299.99,
  "weight": 0.25,
  "warrantyId": "770e8400-e29b-41d4-a716-446655440000",
  "brandId": "880e8400-e29b-41d4-a716-446655440000",
  "categoryIds": [
    "990e8400-e29b-41d4-a716-446655440000",
    "aa0e8400-e29b-41d4-a716-446655440000"
  ]
}
```

**Response:**
```json
{
  "productId": "bb0e8400-e29b-41d4-a716-446655440000",
  "sku": "PROD-001",
  "name": "Wireless Headphones",
  "description": "High-quality wireless headphones with noise cancellation",
  "price": 299.99,
  "weight": 0.25,
  "warrantyId": "770e8400-e29b-41d4-a716-446655440000",
  "brandId": "880e8400-e29b-41d4-a716-446655440000",
  "categoryIds": [
    "990e8400-e29b-41d4-a716-446655440000",
    "aa0e8400-e29b-41d4-a716-446655440000"
  ],
  "reviewIds": []
}
```

### Create Coupon

**Request:**
```http
POST /api/postgresql/coupons/create
Content-Type: application/json

{
  "code": "SUMMER2024",
  "discountType": "percentage",
  "discountValue": 15.00,
  "minimumOrderValue": 100.00,
  "expiryDate": "2024-12-31",
  "usageLimit": 1000,
  "isActive": true
}
```

**Response:**
```json
{
  "couponId": "cc0e8400-e29b-41d4-a716-446655440000",
  "code": "SUMMER2024",
  "discountType": "percentage",
  "discountValue": 15.00,
  "minimumOrderValue": 100.00,
  "expiryDate": "2024-12-31",
  "usageLimit": 1000,
  "timesUsed": 0,
  "isActive": true
}
```

### Create Order

**Request:**
```http
POST /api/postgresql/orders/create
Content-Type: application/json

{
  "userId": "dd0e8400-e29b-41d4-a716-446655440000",
  "address": {
    "street": "Main Street",
    "streetNumber": "123",
    "zip": "2100",
    "city": "Copenhagen"
  },
  "orderProducts": [
    {
      "productId": "bb0e8400-e29b-41d4-a716-446655440000",
      "quantity": 2,
      "warehouseId": "660e8400-e29b-41d4-a716-446655440000"
    }
  ],
  "couponCode": "SUMMER2024"
}
```

**Response:**
```json
{
  "orderId": "ee0e8400-e29b-41d4-a716-446655440000",
  "user": {
    "userId": "dd0e8400-e29b-41d4-a716-446655440000",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  },
  "address": {
    "addressId": "ff0e8400-e29b-41d4-a716-446655440000",
    "street": "Main Street",
    "streetNumber": "123",
    "zip": "2100",
    "city": "Copenhagen"
  },
  "status": "processing",
  "orderProducts": [
    {
      "product": {
        "productId": "bb0e8400-e29b-41d4-a716-446655440000",
        "name": "Wireless Headphones",
        "description": "High-quality wireless headphones with noise cancellation",
        "price": 299.99,
        "weight": 0.25,
        "warrantyId": "770e8400-e29b-41d4-a716-446655440000",
        "brandId": "880e8400-e29b-41d4-a716-446655440000"
      },
      "quantity": 2,
      "unitPrice": 299.99,
      "totalPrice": 599.98
    }
  ]
}
```

---

## Migration Guide

### For API Consumers

#### Breaking Changes

1. **Warehouse Endpoints**
   - Request bodies now require `WarehouseRequestDTO` format
   - Response bodies return `WarehouseResponseDTO` format
   - Address must be referenced by `addressId` (not nested object in request)

2. **Product Endpoints**
   - Request bodies now require `ProductRequestDTO` format
   - `categoryIds` is now a list of UUIDs (not nested Category objects)
   - Response includes `reviewIds` list

3. **Coupon Endpoints**
   - Request bodies now require `CouponRequestDTO` format
   - Response includes `timesUsed` field

4. **Order Endpoints**
   - Request body now requires `OrderRequestDTO` format
   - Address is nested in request (not referenced by ID)
   - Order products use simplified structure with `productId`, `quantity`, and `warehouseId`
   - Response includes nested user and address objects

#### Migration Steps

1. Update request payloads to match new DTO structures
2. Update response parsing to handle new DTO structures
3. For products, convert category objects to categoryIds list
4. For orders, restructure address and order products according to new format

### For Developers

#### Adding New DTOs

1. Create DTO classes in appropriate package under `entity/dto/{domain}/`
2. Add `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` annotations
3. Create mapper class in `entity/mapper/` package
4. Update service methods to accept/return DTOs
5. Update controller endpoints to use DTOs

#### Best Practices

1. **Always use DTOs for API boundaries** - Never expose entities directly
2. **Use mappers for conversions** - Keep mapping logic centralized
3. **Validate DTOs** - Add validation annotations where appropriate
4. **Document nested structures** - Clearly document when DTOs contain other DTOs
5. **Handle nulls gracefully** - Mappers should handle null inputs safely

---

## Benefits of DTO Implementation

1. **API Stability** - Internal entity changes don't break API contracts
2. **Security** - Sensitive fields (like passwords) are never exposed
3. **Performance** - Only necessary data is serialized and transferred
4. **Versioning** - Easier to version APIs by introducing new DTOs
5. **Documentation** - Clear API contracts through DTO structure
6. **Validation** - Centralized validation at DTO level
7. **Flexibility** - Different DTOs for different use cases (request vs response)

---

## Repository Changes

### CouponRepository

Added new method:
- `Optional<Coupon> findByCode(String code)` - Finds coupon by code string

---

## Notes

- All DTOs use Lombok's `@Builder` pattern for fluent object creation
- Enum values are converted to strings in DTOs for better API compatibility
- Nested DTOs are used to represent relationships (e.g., User in Order, Product in OrderProduct)
- Lists of IDs are used instead of full objects where appropriate (e.g., categoryIds, reviewIds)
- All mappers handle null inputs gracefully to prevent NullPointerExceptions

---

## Future Enhancements

Potential improvements for future iterations:

1. **Validation Annotations** - Add Jakarta Bean Validation annotations to DTOs
2. **DTO Versioning** - Implement versioning strategy for API evolution
3. **Pagination DTOs** - Create pagination wrapper DTOs for list endpoints
4. **Error DTOs** - Standardize error response DTOs
5. **OpenAPI Documentation** - Enhance Swagger documentation with DTO examples
6. **Unit Tests** - Add comprehensive unit tests for mappers

---

*Last Updated: 2024*

