import { createContext, useContext, useState, useEffect } from 'react';

const CartContext = createContext(null);

export function CartProvider({ children }) {
    const [cart, setCart] = useState([]);

    // Load cart from localStorage on mount
    useEffect(() => {
        try {
            const raw = localStorage.getItem('cart');
            if (raw) {
                const parsed = JSON.parse(raw);
                if (Array.isArray(parsed)) {
                    const normalized = parsed.map(item => {
                        // Try to extract price from multiple possible formats
                        let price = 0;
                        if (item.price !== undefined && item.price !== null) {
                            price = Number(item.price);
                            if (isNaN(price)) {
                                // Try parsing as string if it's a string representation
                                price = parseFloat(String(item.price)) || 0;
                            }
                        }
                        
                        return {
                            id: String(item.id),
                            name: item.name || 'Unnamed Product',
                            price: price > 0 ? price : 0,
                            quantity: Number(item.quantity) || 0
                        };
                    }).filter(i => i.quantity > 0);
                    setCart(normalized);
                }
            }
        } catch (e) {
            console.warn('Could not load cart from localStorage', e);
        }
    }, []);

    // Save cart to localStorage whenever it changes
    useEffect(() => {
        try {
            localStorage.setItem('cart', JSON.stringify(cart));
        } catch (e) {
            console.warn('Could not save cart to localStorage', e);
        }
    }, [cart]);

    const addToCart = (product) => {
        // Handle both camelCase (productId) and snake_case (product_id) from views
        const pid = String(
            product.id || 
            product.productId || 
            product.product_id ||
            product._id || 
            product.sku
        );
        // Handle both camelCase and snake_case name fields
        const name = product.name || product.productName || product.product_name || product.title || 'Unnamed Product';
        // Try multiple possible price field names and handle different formats
        // Views return product_price (snake_case), regular API returns price (camelCase)
        let price = 0;
        const priceValue = product.price || 
            product.productPrice ||
            product.product_price ||
            product.listPrice || 
            product.list_price;
        
        if (priceValue !== undefined && priceValue !== null) {
            price = Number(priceValue);
            if (isNaN(price)) {
                // Try parsing as string if it's a string representation
                price = parseFloat(String(priceValue)) || 0;
            }
        }
        
        // Debug logging in development
        if (process.env.NODE_ENV === 'development') {
            if (price === 0) {
                console.warn('Adding product to cart with price 0:', { 
                    product, 
                    priceValue, 
                    extractedPrice: price,
                    productKeys: Object.keys(product || {}),
                    allPriceFields: {
                        price: product?.price,
                        listPrice: product?.listPrice,
                        list_price: product?.list_price,
                        product_price: product?.product_price,
                        productPrice: product?.productPrice
                    }
                });
            }
        }

        setCart(prevCart => {
            const existingItem = prevCart.find(item => String(item.id) === pid);
            if (existingItem) {
                // Update quantity and also update price if it was 0 or missing
                return prevCart.map(item =>
                    String(item.id) === pid
                        ? { 
                            ...item, 
                            quantity: item.quantity + 1,
                            // Update price if current price is 0 or missing, but new price is valid
                            price: (item.price && item.price > 0) ? item.price : (price > 0 ? price : item.price)
                        }
                        : item
                );
            } else {
                return [...prevCart, { id: pid, name, price, quantity: 1 }];
            }
        });
    };

    const removeFromCart = (productId) => {
        setCart(prevCart => prevCart.filter(item => String(item.id) !== String(productId)));
    };

    const changeQuantity = (productId, delta) => {
        setCart(prevCart => {
            const item = prevCart.find(i => String(i.id) === String(productId));
            if (!item) return prevCart;

            const newQuantity = item.quantity + delta;
            if (newQuantity <= 0) {
                return prevCart.filter(i => String(i.id) !== String(productId));
            }

            return prevCart.map(i =>
                String(i.id) === String(productId)
                    ? { ...i, quantity: newQuantity }
                    : i
            );
        });
    };

    const clearCart = () => {
        setCart([]);
    };

    const getTotalItems = () => {
        return cart.reduce((sum, item) => sum + item.quantity, 0);
    };

    const getTotalPrice = () => {
        return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    };

    const value = {
        cart,
        addToCart,
        removeFromCart,
        changeQuantity,
        clearCart,
        getTotalItems,
        getTotalPrice,
    };

    return (
        <CartContext.Provider value={value}>
            {children}
        </CartContext.Provider>
    );
}

export function useCart() {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error('useCart must be used within a CartProvider');
    }
    return context;
}

