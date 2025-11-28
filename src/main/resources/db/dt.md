# Warehouse:
## Request
{
    "name": "string",
    "phoneNumber": "string",
    "addressId": "UUID"
}

## Response
{
    "warehouseId": "UUID",
    "name": "string",
    "phoneNumber": "string",
    "addressId": "UUID"
}

# Product:
## Request
{
    "sku": "string",
    "name": "string",
    "description": "string",
    "price": 100.00,
    "weight": 100.00,
    "warrantyId": "UUID",
    "brandId": "UUID",
    "categoryIds": [
        "UUID"
    ]
}

## Response
{
    "productId": "UUID",
    "sku": "string",
    "name": "string",
    "description": "string",
    "price": 100.00,
    "weight": 100.00,
    "warrantyId": "UUID",
    "brandId": "UUID",
    "categoryIds": [
        "UUID"
    ],
    "reviewIds": [
        "UUID"
    ]
}

# Coupon:
## Request
{
    "code": "string",
    "discountType": "percentage",
    "discountValue": 10.00,
    "minimumOrderValue": 50.00,
    "expiryDate": "2024-06-01",
    "usageLimit": 100,
    "isActive": true
}

## Response
{
    "couponId": "UUID",
    "code": "string",
    "discountType": "percentage",
    "discountValue": 10.00,
    "minimumOrderValue": 50.00,
    "expiryDate": "2024-06-01",
    "usageLimit": 100,
    "timesUsed": 5,
    "isActive": true
}

# Order:
## Request
{
    "userId": "UUID",
    "address": { 
        "street": "string",
        "streetNumber": "string",
        "zip": "string",
        "city": "string"
    },
    "orderProducts": [
        {
            "productId": "UUID",
            "quantity": 2,
            "warehouseId": "UUID"
        }
    ],
    "couponCode": "string"
}

## Response
{
    "orderId": "UUID",
    "user": {
        "userId": "UUID",
        "firstName": "string",
        "lastName": "string",
        "email": "string"
    },
    "address": { 
        "addressId": "UUID",
        "street": "string",
        "streetNumber": "string",
        "zip": "string",
        "city": "string"
    },
    "status": "processing",
    "orderProducts": [
        {
            "product": {
                "productId": "UUID",
                "name": "string",
                "description": "string",
                "price": 100.00,
                "weight": 100.00,
                "warrantyId": "UUID",
                "brandId": "UUID"
            },
            "quantity": 2,
            "totalPrice": 200.00
        }
    ]
}
    

# Order_Product

## Request
{
    "orderId": "UUID",
    "productId": "UUID",
    "quantity": 2,
    "unitPrice": 100.00
}

## Response (used in order response)
{
    "product": {
        "productId": "UUID",
        "name": "string",
        "description": "string",
        "price": 100.00,
        "weight": 100.00,
        "warrantyId": "UUID",
        "brandId": "UUID"
    },
    "quantity": 2,
    "unitPrice": 100.00,
    "totalPrice": 200.00
}

# User:
## Response
{
    "userId": "UUID",
    "firstName": "string",
    "lastName": "string",
    "email": "string",
    "phoneNumber": "string",
    "dateOfBirth": "2000-01-01",
    "isAdmin": false
}
