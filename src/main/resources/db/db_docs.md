# Views:
## Materialized View:
### Best Selling Products View
Lists top 10 products by sales volume in the last month, including product name, total units sold, and revenue generated.
### Best reviewed Products View
Lists top 10 products by average customer rating, including product name, average rating, and number of reviews.

# Stored Procedure:
### Place Order Procedure
validates coupon codes, calculates discounts, creates order records, generates order items, updates inventory quantities, and creates payment records. Ensures all steps succeed or roll back entirely.

# Stored Functions:
### Calculate Order Total Function (Before order placement)
Takes order ID as input and returns the final amount after applying subtotal, taxes (based on shipping address), shipping costs, and any applicable coupon discounts.

### Check Product Availability Function (Used inside Place Order Procedure)
Returns boolean indicating whether sufficient stock exists for a given product and quantity. Used during checkout validation and shopping cart updates.

### Validate Coupon Function (Used inside Apply Coupon Procedure)
Returns discount amount for a given coupon code and order. Checks expiration and usage limits

### Get Product Rating
Returns average customer rating for a specified product based on reviews.

# Triggers:
### Payment Status Trigger (AFTER UPDATE on payment)
When payment status changes to "COMPLETED", updates corresponding order status to "CONFIRMED" and sends order to fulfillment queue.

### Order gets cancelled (AFTER UPDATE on order)
When an order status is updated to "CANCELLED", restores reserved inventory quantities for all associated order items.

# Events

### Reindex all Indexes Event
Runs daily at 2 AM to reindex all database indexes to maintain optimal query performance.

### Order Abandonment Follow-up Event
Runs every 1 hour to identify orders in "PENDING" status, marks them as abandoned, and releases reserved inventory back to available stock.

### Warranty Expiration Notice Event (IS NOT IMPLEMENTED, ONLY EXAMPLE)
Runs daily to identify warranties expiring within 30 days and generates customer notification lists for extended warranty offers.

### Monthly Sales Report Event (IS NOT IMPLEMENTED, ONLY EXAMPLE)
Runs on the first day of each month to aggregate previous month's sales data by category, brand, and region. Populates reporting tables used by analytics dashboards.

# Index:

### Index BestSelling Products View on product_name, product_price
Speeds up queries filtering or sorting by product name or price in the Best Selling Products View.

### Index BestReviewed Products View on average_rating DESC
Optimizes retrieval of top-rated products by average customer rating in descending order.

### Index product on product_id
Enhances lookup speed for product details based on unique product identifiers.

### Index product on category_id
Improves performance of product searches and filters based on category.

### Index product on brand_id
Facilitates faster queries for products associated with specific brands.


## Test Audit
/*
-- Test 1: Insert a user (will be logged as INSERT)
INSERT INTO "user" (user_id, email, first_name, last_name, phone_number, date_of_birth, password)
VALUES (uuid_generate_v4(), 'test@test.com', 'Test', 'User', '12345678', '1990-01-01', 'hashed');

-- Test 2: Update the user (will be logged as UPDATE)
UPDATE "user" SET email = 'newemail@test.com' WHERE email = 'test@test.com';

-- Test 3: Soft delete the user (will be logged as SOFT_DELETE)
UPDATE "user" SET deleted_at = NOW() WHERE email = 'newemail@test.com';

-- View audit log
SELECT * FROM vw_recent_audit;

-- View only soft deletes
SELECT * FROM vw_soft_deletes;

-- Get history for specific user
SELECT * FROM fn_get_audit_history('user', 'user-uuid-here');
*/