CREATE EXTENSION IF NOT EXISTS pg_cron;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE brand (
                       brand_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name TEXT NOT NULL,
                       description TEXT NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP NULL
);

CREATE TABLE category (
                          category_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          name TEXT NOT NULL,
                          description TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL
);

-- ======================
-- Users
-- ======================
CREATE TABLE "user" (
                        user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        first_name TEXT NOT NULL,
                        last_name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        phone_number TEXT NOT NULL,
                        date_of_birth DATE NOT NULL,
                        password TEXT NOT NULL,
                        is_admin BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP NULL
);

-- ======================
-- Address
-- ======================
CREATE TABLE address (
                         address_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         user_id UUID NOT NULL REFERENCES "user"(user_id),
                         street TEXT NOT NULL,
                         street_number TEXT NOT NULL,
                         zip TEXT NOT NULL,
                         city TEXT NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL,
                         FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);

CREATE TABLE warehouse (
                           warehouse_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name TEXT NOT NULL,
                           phone_number TEXT NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           deleted_at TIMESTAMP NULL,
                           address_id UUID NOT NULL REFERENCES address(address_id)
);

-- ======================
-- Coupon
-- ======================
CREATE TYPE discount_type_enum AS ENUM ('percentage', 'fixed_amount');
CREATE TABLE coupon (
                        coupon_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        code TEXT NOT NULL UNIQUE,
                        discount_type discount_type_enum NOT NULL,
                        discount_value NUMERIC(12, 2) NOT NULL,
                        minimum_order_value NUMERIC(12, 2) NOT NULL,
                        expiry_date DATE,
                        usage_limit INTEGER NOT NULL,
                        times_used INTEGER DEFAULT 0,
                        is_active BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP NULL
);

-- ======================
-- Warranty
-- ======================
CREATE TABLE warranty (
                          warranty_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          start_date DATE NOT NULL,
                          end_date DATE NOT NULL,
                          description TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL
);

-- ======================
-- Product
-- ======================
CREATE TABLE product (
                         product_id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
                         sku TEXT UNIQUE NOT NULL,
                         name TEXT NOT NULL,
                         description TEXT NOT NULL,
                         price NUMERIC(12, 2) NOT NULL,
                         weight NUMERIC(8, 2),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL,
                         brand_id UUID NOT NULL REFERENCES brand(brand_id),
                         warranty_id UUID NOT NULL REFERENCES warranty(warranty_id)
);

-- ======================
-- Orders
-- ======================
CREATE TYPE order_type_enum AS ENUM (
  'pending', 'confirmed', 'processing',
  'shipped', 'delivered', 'cancelled',
  'returned'
);

CREATE TABLE "order" (
                         order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         user_id UUID NOT NULL REFERENCES "user"(user_id),
                         address_id UUID REFERENCES address(address_id),
                         coupon_id UUID REFERENCES coupon(coupon_id),
                         order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         status order_type_enum DEFAULT 'pending',
                         total_amount NUMERIC(12, 2) NOT NULL,
                         subtotal NUMERIC(12, 2) NOT NULL,
                         tax_amount NUMERIC(12, 2) NOT NULL,
                         shipping_cost NUMERIC(12, 2) NOT NULL,
                         discount_amount NUMERIC(12, 2),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL
);

-- ======================
-- Payments
-- ======================
CREATE TYPE payment_type_enum AS ENUM(
  'credit_card', 'paypal', 'bank', 'klarna',
  'cash'
);

CREATE TYPE status_type_enum AS ENUM(
  'pending', 'completed', 'failed',
  'refunded'
);

CREATE TABLE payment (
                         payment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         order_id UUID NOT NULL REFERENCES "order"(order_id),
                         payment_method payment_type_enum NOT NULL,
                         amount NUMERIC(12, 2) NOT NULL,
                         status status_type_enum DEFAULT 'pending',
                         payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL
);

-- ======================
-- Review
-- ======================
CREATE TABLE review (
                        review_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        product_id UUID NOT NULL REFERENCES product(product_id),
                        user_id UUID NOT NULL REFERENCES "user"(user_id),
                        order_id UUID NOT NULL REFERENCES "order"(order_id),
                        review_value INTEGER CHECK (
                            review_value >= 1
                                AND review_value <= 5
                            ),
                        title TEXT NOT NULL,
                        comment TEXT NOT NULL,
                        is_verified_purchase BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP NULL
);

-- ======================
-- Join Tables
-- ======================
-- Order-Product many-to-many relationship (order line items)
CREATE TABLE order_product (
                               order_id UUID NOT NULL REFERENCES "order"(order_id) ON DELETE CASCADE,
                               product_id UUID NOT NULL REFERENCES product(product_id) ON DELETE RESTRICT,
                               quantity INTEGER NOT NULL CHECK (quantity > 0),
                               unit_price NUMERIC(12, 2) NOT NULL,
                               total_price NUMERIC(12, 2) NOT NULL,
                               PRIMARY KEY (order_id, product_id)
);

-- Warehouse-Product many-to-many relationship with stock tracking
CREATE TABLE warehouse_product (
                                   warehouse_id UUID NOT NULL REFERENCES warehouse(warehouse_id) ON DELETE CASCADE,
                                   product_id UUID NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
                                   stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
                                   PRIMARY KEY (warehouse_id, product_id)
);

CREATE TABLE product_category (
                                  product_id UUID NOT NULL,
                                  category_id UUID NOT NULL,
                                  PRIMARY KEY (product_id, category_id),
                                  FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
                                  FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
);