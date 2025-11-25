// API Configuration
const API_BASE_URL = '/api';
const API_ENDPOINTS = {
    AUTH: `${API_BASE_URL}/postgresql/auth`,
    PRODUCTS: `${API_BASE_URL}/postgresql/products`,
    CATEGORIES: `${API_BASE_URL}/postgresql/categories`,
    BRANDS: `${API_BASE_URL}/postgresql/brands`,
    WAREHOUSES: `${API_BASE_URL}/postgresql/warehouses`,
    REVIEWS: `${API_BASE_URL}/postgresql/reviews`,
    STRIPE: `${API_BASE_URL}/postgresql/stripe`
};

// Global State
let currentUser = null;
let products = [];
let categories = [];
let brands = [];
let cart = [];
// Map to hold synthetic id -> product when product has no stable id field
const productIndexMap = {};

// DOM Elements
const authModal = document.getElementById('auth-modal');
const authLink = document.getElementById('auth-link');
const hamburger = document.getElementById('hamburger');
const navMenu = document.getElementById('nav-menu');
const cartCount = document.getElementById('cart-count');
const cartIcon = document.getElementById('cart-icon');
const cartModal = document.getElementById('cart-modal');
const cartItemsContainer = document.getElementById('cart-items');
const cartTotalEl = document.getElementById('cart-total');
const continueShoppingBtn = document.getElementById('continue-shopping-btn');
const checkoutBtn = document.getElementById('checkout-btn');

// Initialize App
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
});

// App Initialization
function initializeApp() {
    checkAuthStatus();
    loadCart();
    loadProducts();
    loadCategories();
    loadBrands();
    updateCartCount();
}

// Cart persistence
function saveCart() {
    try {
        localStorage.setItem('cart', JSON.stringify(cart));
    } catch (e) {
        console.warn('Could not save cart to localStorage', e);
    }
}

function loadCart() {
    try {
        const raw = localStorage.getItem('cart');
        if (raw) {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed)) {
                // Normalize stored cart: ensure id is string and numeric fields are numbers
                cart = parsed.map(item => ({
                    id: String(item.id),
                    name: item.name || 'Unnamed Product',
                    price: Number(item.price) || 0,
                    quantity: Number(item.quantity) || 0
                })).filter(i => i.quantity > 0);
            }
        }
    } catch (e) {
        console.warn('Could not load cart from localStorage', e);
        cart = [];
    }
}

// Update the cart count badge in the UI
function updateCartCount() {
    const totalItems = (cart || []).reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
    if (cartCount) {
        cartCount.textContent = totalItems;
        cartCount.style.display = totalItems > 0 ? 'flex' : 'none';
    }
}

// Make the updater available globally (prevents "not defined" in some runtime contexts)
if (typeof window !== 'undefined') window.updateCartCount = updateCartCount;

// Event Listeners Setup
function setupEventListeners() {
    // Re-query DOM elements inside the function (they may be null when script parsed)
    const hamburgerEl = document.getElementById('hamburger');
    const navMenuEl = document.getElementById('nav-menu');
    const cartIconEl = document.getElementById('cart-icon');
    const cartModalEl = document.getElementById('cart-modal');
    const continueBtnEl = document.getElementById('continue-shopping-btn');
    const checkoutBtnEl = document.getElementById('checkout-btn');
    const authLinkEl = document.getElementById('auth-link');
    const loginFormEl = document.getElementById('login-form');
    const registerFormEl = document.getElementById('register-form');
    const searchInputEl = document.getElementById('search-input');
    const categoryFilterEl = document.getElementById('category-filter');
    const brandFilterEl = document.getElementById('brand-filter');
    const ordersLink = document.getElementById('orders-link');

    // Mobile Navigation
    if (hamburgerEl && navMenuEl) {
        hamburgerEl.addEventListener('click', () => navMenuEl.classList.toggle('active'));
    }

    // Cart Icon -> open/close
    if (cartIconEl) {
        cartIconEl.addEventListener('click', (e) => {
            e.preventDefault();
            toggleCartModal();
        });
    }

    // Cart modal outside click & close button
    if (cartModalEl) {
        cartModalEl.addEventListener('click', (e) => {
            if (e.target === cartModalEl) hideCartModal();
        });
        const cartClose = cartModalEl.querySelector('.close');
        if (cartClose) cartClose.addEventListener('click', hideCartModal);
    }

    // Continue / Checkout
    if (continueBtnEl) continueBtnEl.addEventListener('click', () => { hideCartModal(); scrollToSection('products'); });
    if (checkoutBtnEl) checkoutBtnEl.addEventListener('click', () => {
        if (!currentUser) { showAlert('Please login to proceed to checkout', 'error'); showAuthModal(); return; }

        // Start Stripe checkout process
        (async () => {
            try {
                if (cart.length === 0) {
                    showAlert('Your cart is empty', 'error');
                    return;
                }

                showAlert('Creating checkout session...', 'info');

                const response = await fetch(`${API_ENDPOINTS.STRIPE}/checkout`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    credentials: 'same-origin', // Include session cookies
                    body: JSON.stringify({
                        cart: cart,
                        successUrl: window.location.origin + '/?checkout=success',
                        cancelUrl: window.location.origin + '/?checkout=cancel'
                    })
                });

                const data = await response.json();

                if (!response.ok) {
                    console.error('Stripe checkout error:', data);
                    showAlert(data.error || 'Failed to create checkout session', 'error');
                    return;
                }

                if (data.url) {
                    // Clear cart on successful checkout initiation
                    cart = [];
                    _onCartChanged();
                    hideCartModal();

                    // Redirect to Stripe checkout
                    window.location.href = data.url;
                } else {
                    showAlert('No checkout URL received', 'error');
                }

            } catch (error) {
                console.error('Checkout error:', error);
                showAlert('Failed to initiate checkout. Please try again.', 'error');
            }
        })();
    });

    // Auth link
    if (authLinkEl) {
        authLinkEl.addEventListener('click', (e) => { e.preventDefault(); if (currentUser) logout(); else showAuthModal(); });
    }

    // Global close for auth modal
    const closeButton = document.querySelector('.close');
    if (closeButton) closeButton.addEventListener('click', hideAuthModal);

    // Click outside auth modal
    window.addEventListener('click', (e) => {
        const authModalEl = document.getElementById('auth-modal');
        if (e.target === authModalEl) hideAuthModal();
    });

    // Escape key closes modals
    window.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') { hideAuthModal(); hideCartModal(); }
    });

    // Forms
    if (loginFormEl) loginFormEl.addEventListener('submit', handleLogin);
    if (registerFormEl) registerFormEl.addEventListener('submit', handleRegister);

    // Search & filters
    if (searchInputEl) searchInputEl.addEventListener('input', filterProducts);
    if (categoryFilterEl) categoryFilterEl.addEventListener('change', filterProducts);
    if (brandFilterEl) brandFilterEl.addEventListener('change', filterProducts);

    // Smooth scrolling links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    });

    // Handle orders navigation
    if (ordersLink) {
        ordersLink.addEventListener('click', function(e) {
            e.preventDefault();
            if (currentUser) {
                showOrdersSection();
                loadUserOrders();
            } else {
                showAlert('Please login to view orders', 'error');
                showAuthModal();
            }
        });
    }
}

// Authentication Functions
function checkAuthStatus() {
    // Check server-side authentication status instead of localStorage
    fetch(`${API_ENDPOINTS.AUTH}/current-user`, {
        method: 'GET',
        credentials: 'same-origin' // Include session cookies
    })
    .then(response => response.json())
    .then(data => {
        if (data.user && data.isAuthenticated) {
            currentUser = data.user;
            updateAuthUI();
        } else {
            currentUser = null;
            updateAuthUI();
        }
    })
    .catch(error => {
        console.error('Error checking auth status:', error);
        currentUser = null;
        updateAuthUI();
    });
}

function showAuthModal() {
    if (authModal) {
        authModal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
}

function hideAuthModal() {
    if (authModal) {
        authModal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

function showTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active');
    });

    // Show selected tab
    document.getElementById(`${tabName}-tab`).classList.add('active');

    // Find and activate the correct button
    const buttons = document.querySelectorAll('.tab-button');
    buttons.forEach((btn, index) => {
        if ((tabName === 'login' && index === 0) || (tabName === 'register' && index === 1)) {
            btn.classList.add('active');
        }
    });
}

async function handleLogin(e) {
    e.preventDefault();

    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_ENDPOINTS.AUTH}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'same-origin', // Include session cookies
            body: JSON.stringify({ email, password })
        });

        let responseData;
        const contentType = response.headers.get('content-type');

        // Check if response is JSON
        if (contentType && contentType.includes('application/json')) {
            responseData = await response.json();
        } else {
            // If not JSON, treat as text
            const responseText = await response.text();
            responseData = { message: responseText || 'Login failed' };
        }

        if (response.ok && responseData.success) {
            // Use server-side session instead of localStorage
            currentUser = responseData.user;
            updateAuthUI();
            hideAuthModal();
            showAlert('Login successful!', 'success');
            // Clear the form
            document.getElementById('login-form').reset();
        } else {
            showAlert(responseData.message || 'Login failed', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('Login failed. Please try again.', 'error');
    }
}

async function handleRegister(e) {
    e.preventDefault();

    const firstName = document.getElementById('register-firstName').value.trim();
    const lastName = document.getElementById('register-lastName').value.trim();
    const email = document.getElementById('register-email').value.trim();
    const phoneNumber = document.getElementById('register-phone').value.trim();
    const password = document.getElementById('register-password').value;

    // Basic validation
    if (!firstName || !lastName || !email || !phoneNumber || !password) {
        showAlert('Please fill in all fields', 'error');
        return;
    }

    if (password.length < 6) {
        showAlert('Password must be at least 6 characters long', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_ENDPOINTS.AUTH}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ firstName, lastName, email, phoneNumber, password })
        });

        let responseData;
        const contentType = response.headers.get('content-type');

        // Check if response is JSON
        if (contentType && contentType.includes('application/json')) {
            responseData = await response.json();
        } else {
            // If not JSON, treat as text
            const responseText = await response.text();
            responseData = { message: responseText || 'Registration failed' };
        }

        console.log('Registration response:', response.status, responseData); // Debug log

        if (response.ok) {
            showAlert('Registration successful! Please login.', 'success');
            showTab('login');
            // Clear the form
            document.getElementById('register-form').reset();
        } else {
            showAlert(responseData.message || 'Registration failed', 'error');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showAlert('Registration failed. Please try again.', 'error');
    }
}

function logout() {
    // Call server-side logout
    fetch(`${API_ENDPOINTS.AUTH}/logout`, {
        method: 'DELETE',
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentUser = null;
            updateAuthUI();
            cart = [];
            updateCartCount();
            showAlert('Logged out successfully', 'success');
        } else {
            showAlert('Logout failed', 'error');
        }
    })
    .catch(error => {
        console.error('Logout error:', error);
        // Even if server logout fails, clear client state
        currentUser = null;
        updateAuthUI();
        cart = [];
        updateCartCount();
        showAlert('Logged out', 'info');
    });
}

function updateAuthUI() {
    if (currentUser) {
        // Handle different user data structures from login/register responses
        const firstName = currentUser.firstName || (currentUser.user && currentUser.user.firstName);
        const email = currentUser.email || (currentUser.user && currentUser.user.email);

        authLink.textContent = 'Logout';
        authLink.title = `Logged in as ${firstName || email}`;

        // Show My Orders link
        const ordersLink = document.getElementById('orders-link');
        if (ordersLink) ordersLink.style.display = 'inline';
    } else {
        authLink.textContent = 'Login';
        authLink.title = 'Click to login';

        // Hide My Orders link and section
        const ordersLink = document.getElementById('orders-link');
        const ordersSection = document.getElementById('orders');
        if (ordersLink) ordersLink.style.display = 'none';
        if (ordersSection) ordersSection.style.display = 'none';
    }
}

// Orders Functions
async function loadUserOrders() {
    if (!currentUser) {
        showError('orders-grid', 'Please login to view your orders');
        return;
    }

    try {
        showLoading('orders-grid');
        const response = await fetch(`${API_ENDPOINTS.STRIPE}/orders`, {
            method: 'GET',
            credentials: 'same-origin' // Include session cookies
        });

        if (response.ok) {
            const orders = await response.json();
            displayOrders(orders);
        } else {
            throw new Error('Failed to load orders');
        }
    } catch (error) {
        console.error('Error loading orders:', error);
        showError('orders-grid', 'Failed to load orders');
    }
}

function displayOrders(orders) {
    const grid = document.getElementById('orders-grid');

    if (orders.length === 0) {
        grid.innerHTML = '<div class="no-orders"><p>You haven\'t placed any orders yet.</p><button class="btn btn-primary" onclick="scrollToSection(\'products\')">Start Shopping</button></div>';
        return;
    }

    grid.innerHTML = orders.map(order => {
        const orderDate = new Date(order.createdAt).toLocaleDateString();
        const totalItems = order.cart.reduce((sum, item) => sum + item.quantity, 0);
        const totalAmount = order.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

        return `
            <div class="order-card">
                <div class="order-header">
                    <div class="order-info">
                        <h3>Order #${order.orderId.substring(0, 8)}</h3>
                        <p class="order-date">${orderDate}</p>
                        <span class="order-status status-${order.status.toLowerCase()}">${order.status}</span>
                    </div>
                    <div class="order-summary">
                        <p class="order-total">$${totalAmount.toFixed(2)}</p>
                        <p class="order-items">${totalItems} items</p>
                    </div>
                </div>
                <div class="order-items-list">
                    ${order.cart.map(item => `
                        <div class="order-item">
                            <span class="item-name">${item.name}</span>
                            <span class="item-quantity">Qty: ${item.quantity}</span>
                            <span class="item-price">$${item.price.toFixed(2)}</span>
                        </div>
                    `).join('')}
                </div>
                <div class="order-actions">
                    <button class="btn btn-secondary" onclick="viewOrderDetails('${order.orderId}')">
                        <i class="fas fa-eye"></i> View Details
                    </button>
                    ${order.status === 'PENDING' ? 
                        `<button class="btn btn-primary" onclick="reorderItems('${order.orderId}')">
                            <i class="fas fa-redo"></i> Reorder
                        </button>` : ''
                    }
                </div>
            </div>
        `;
    }).join('');
}

function viewOrderDetails(orderId) {
    showAlert(`Order details for ${orderId.substring(0, 8)} - Feature coming soon!`, 'info');
}

function reorderItems(orderId) {
    // This would add the order items back to cart
    showAlert('Items added to cart!', 'success');
}

function showOrdersSection() {
    // Hide all other sections
    document.querySelectorAll('.section').forEach(section => {
        section.style.display = 'none';
    });

    // Show orders section
    const ordersSection = document.getElementById('orders');
    if (ordersSection) {
        ordersSection.style.display = 'block';
    }
}

// Data Loading Functions
async function loadProducts() {
    try {
        showLoading('products-grid');
        const response = await fetch(API_ENDPOINTS.PRODUCTS);

        if (response.ok) {
            products = await response.json();
            displayProducts(products);
        } else {
            throw new Error('Failed to load products');
        }
    } catch (error) {
        console.error('Error loading products:', error);
        showError('products-grid', 'Failed to load products');
    }
}

async function loadCategories() {
    try {
        const response = await fetch(API_ENDPOINTS.CATEGORIES);

        if (response.ok) {
            categories = await response.json();
            displayCategories(categories);
            populateCategoryFilter(categories);
        } else {
            throw new Error('Failed to load categories');
        }
    } catch (error) {
        console.error('Error loading categories:', error);
        showError('categories-grid', 'Failed to load categories');
    }
}

async function loadBrands() {
    try {
        const response = await fetch(API_ENDPOINTS.BRANDS);

        if (response.ok) {
            brands = await response.json();
            displayBrands(brands);
            populateBrandFilter(brands);
        } else {
            throw new Error('Failed to load brands');
        }
    } catch (error) {
        console.error('Error loading brands:', error);
        showError('brands-grid', 'Failed to load brands');
    }
}

// Display Functions
function displayProducts(productsToShow) {
    const grid = document.getElementById('products-grid');

    if (productsToShow.length === 0) {
        grid.innerHTML = '<p class="no-results">No products found</p>';
        return;
    }

    // Clear the previous index map
    for (const k in productIndexMap) delete productIndexMap[k];

    grid.innerHTML = productsToShow.map((product, idx) => {
        let pid = resolveProductIdField(product);
        if (!pid) {
            // create a synthetic stable id for this render
            pid = `__idx_${idx}`;
            productIndexMap[pid] = product;
        } else {
            // also map to product for quick lookup
            productIndexMap[pid] = product;
        }
        const safeName = (product.name || 'Unnamed Product').replace(/"/g, '&quot;');
        const priceVal = (product.price !== undefined && product.price !== null) ? product.price : 0;
        return `
        <div class="product-card" data-product-id="${pid}" data-product-name="${safeName}" data-product-price="${priceVal}">
            <div class="product-image">
                <i class="fas fa-mobile-alt"></i>
            </div>
            <h3 class="product-title">${product.name || 'Unnamed Product'}</h3>
            <p class="product-description">${product.description || 'No description available'}</p>
            <div class="product-price">$${priceVal}</div>
            <div class="product-actions">
                <button class="btn btn-primary" onclick="addToCart('${pid}')">
                    <i class="fas fa-cart-plus"></i> Add to Cart
                </button>
                <button class="btn btn-secondary" onclick="viewProduct('${pid}')">
                    <i class="fas fa-eye"></i> View
                </button>
            </div>
        </div>
    `}).join('');
}

function displayCategories(categoriesToShow) {
    const grid = document.getElementById('categories-grid');

    if (categoriesToShow.length === 0) {
        grid.innerHTML = '<p class="no-results">No categories found</p>';
        return;
    }

    grid.innerHTML = categoriesToShow.map(category => `
        <div class="category-card" onclick="filterByCategory('${category.id}')">
            <div class="category-icon">
                <i class="fas fa-tag"></i>
            </div>
            <h3 class="category-name">${category.name || 'Unnamed Category'}</h3>
        </div>
    `).join('');
}

function displayBrands(brandsToShow) {
    const grid = document.getElementById('brands-grid');

    if (brandsToShow.length === 0) {
        grid.innerHTML = '<p class="no-results">No brands found</p>';
        return;
    }

    grid.innerHTML = brandsToShow.map(brand => `
        <div class="brand-card" onclick="filterByBrand('${brand.id}')">
            <div class="brand-logo">
                <i class="fas fa-building"></i>
            </div>
            <h3 class="brand-name">${brand.name || 'Unnamed Brand'}</h3>
        </div>
    `).join('');
}

// Filter Functions
function populateCategoryFilter(categories) {
    const select = document.getElementById('category-filter');
    const options = categories.map(cat =>
        `<option value="${cat.id}">${cat.name || 'Unnamed Category'}</option>`
    ).join('');
    select.innerHTML = '<option value="">All Categories</option>' + options;
}

function populateBrandFilter(brands) {
    const select = document.getElementById('brand-filter');
    const options = brands.map(brand =>
        `<option value="${brand.id}">${brand.name || 'Unnamed Brand'}</option>`
    ).join('');
    select.innerHTML = '<option value="">All Brands</option>' + options;
}

function filterProducts() {
    const searchTerm = document.getElementById('search-input').value.toLowerCase();
    const selectedCategory = document.getElementById('category-filter').value;
    const selectedBrand = document.getElementById('brand-filter').value;

    let filteredProducts = products.filter(product => {
        const matchesSearch = !searchTerm ||
            (product.name && product.name.toLowerCase().includes(searchTerm)) ||
            (product.description && product.description.toLowerCase().includes(searchTerm));

        const matchesCategory = !selectedCategory ||
            (product.category && product.category.id === selectedCategory);

        const matchesBrand = !selectedBrand ||
            (product.brand && product.brand.id === selectedBrand);

        return matchesSearch && matchesCategory && matchesBrand;
    });

    displayProducts(filteredProducts);
}

function filterByCategory(categoryId) {
    document.getElementById('category-filter').value = categoryId;
    scrollToSection('products');
    filterProducts();
}

function filterByBrand(brandId) {
    document.getElementById('brand-filter').value = brandId;
    scrollToSection('products');
    filterProducts();
}

// Cart Functions
function addToCart(productId) {
    // Normalize ids to string to avoid mismatch between numeric IDs and string IDs
    const pid = String(productId);
    // First try to find the original product object from loaded products using flexible matching
    const product = findProductById(pid);
    console.debug('addToCart called for', pid, 'found product via product list:', product);
    if (product) {
        const existingItem = cart.find(item => String(item.id) === pid);
        if (existingItem) {
            existingItem.quantity = Number(existingItem.quantity || 0) + 1;
        } else {
            // Store a minimal product snapshot in cart to keep localStorage small and stable
            const name = product.name || product.productName || product.title || 'Unnamed Product';
            const price = Number(product.price || product.listPrice || 0) || 0;
            cart.push({ id: pid, name, price, quantity: 1 });
        }
        _onCartChanged();
        // If cart modal is open, re-render so the user sees the change immediately
        if (cartModal && cartModal.style.display === 'block') renderCartItems();
        showAlert(`${product.name || product.productName || 'Item'} added to cart`, 'success');
        return;
    }

    // Fallback: try to read the rendered product card from the DOM (in case products haven't loaded yet)
    const card = document.querySelector(`.product-card[data-product-id="${pid}"]`);
    if (card) {
        const nameEl = card.querySelector('.product-title');
        const priceEl = card.querySelector('.product-price');
        const name = nameEl ? nameEl.textContent.trim() : (card.dataset.productName || 'Unnamed Product');
        let price = 0;
        if (priceEl) {
            const txt = priceEl.textContent.replace(/[^0-9.]/g, '');
            price = parseFloat(txt) || Number(card.dataset.productPrice) || 0;
        } else {
            price = Number(card.dataset.productPrice) || 0;
        }
        const existingItem = cart.find(item => String(item.id) === pid);
        if (existingItem) {
            existingItem.quantity = Number(existingItem.quantity || 0) + 1;
        } else {
            cart.push({ id: pid, name, price, quantity: 1 });
        }
        _onCartChanged();
        if (cartModal && cartModal.style.display === 'block') renderCartItems();
        showAlert(`${name} added to cart`, 'success');
        return;
    }

    showAlert('Product not found (maybe still loading)', 'error');
}

// Helper: resolve product id from product object
function resolveProductIdField(product) {
    if (!product) return undefined;
    // Try common id fields
    if (product.id !== undefined && product.id !== null) return String(product.id);
    if (product.productId !== undefined && product.productId !== null) return String(product.productId);
    if (product._id !== undefined && product._id !== null) return String(product._id);
    if (product.sku !== undefined && product.sku !== null) return String(product.sku);
    return undefined;
}

// Helper: find product by flexible id matching
function findProductById(pid) {
    if (!pid) return undefined;
    const ps = products || [];
    // check direct product list fields
    const found = ps.find(p => {
        const pids = [p.id, p.productId, p._id, p.sku];
        return pids.some(x => x !== undefined && String(x) === String(pid));
    });
    if (found) return found;
    // check synthetic/index map
    if (productIndexMap[pid]) return productIndexMap[pid];
    return undefined;
}

// Toggle cart modal visibility
function toggleCartModal() {
    const modalEl = document.getElementById('cart-modal');
    console.debug('toggleCartModal called, modalEl=', modalEl);
    if (!modalEl) return;

    const isVisible = modalEl.style.display === 'block';
    modalEl.style.display = isVisible ? 'none' : 'block';
    document.body.style.overflow = isVisible ? 'auto' : 'hidden';

    if (!isVisible) {
        // Modal is being opened, render items
        renderCartItems();
    }
}

function showCartModal() {
    const modalEl = document.getElementById('cart-modal');
    console.debug('showCartModal called, modalEl=', modalEl);
    if (!modalEl) return;
    renderCartItems();
    modalEl.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

function hideCartModal() {
    const modalEl = document.getElementById('cart-modal');
    console.debug('hideCartModal called, modalEl=', modalEl);
    if (!modalEl) return;
    modalEl.style.display = 'none';
    document.body.style.overflow = 'auto';
}

function renderCartItems() {
    const modalEl = document.getElementById('cart-modal');
    const itemsContainer = document.getElementById('cart-items');
    const totalEl = document.getElementById('cart-total');
    console.debug('renderCartItems called, modalEl=', modalEl, 'itemsContainer=', itemsContainer);

    if (!itemsContainer || !totalEl) return;

    // Clear existing items
    itemsContainer.innerHTML = '';

    if (!Array.isArray(cart) || cart.length === 0) {
        itemsContainer.innerHTML = '<p class="no-results">Your cart is empty</p>';
        totalEl.textContent = '$0.00';
        return;
    }

    let total = 0;

    // Render each cart item
    cart.forEach(item => {
        const product = findProductById(item.id) || {};
        const price = Number(item.price || product.price || 0) || 0;
        const name = item.name || product.name || 'Unnamed Product';
        const quantity = Number(item.quantity) || 0;

        total += price * quantity;

        const itemEl = document.createElement('div');
        itemEl.classList.add('cart-item');
        itemEl.dataset.productId = item.id;

        itemEl.innerHTML = `
            <div class="cart-item-info">
                <h4 class="cart-item-name">${name}</h4>
                <div class="cart-item-details">
                    <span class="cart-item-price">$${price.toFixed(2)}</span>
                    <span class="cart-item-quantity">Qty: ${quantity}</span>
                </div>
            </div>
            <div class="cart-item-actions">
                <button class="btn btn-secondary" onclick="changeQuantity('${item.id}', -1)">
                    <i class="fas fa-minus"></i>
                </button>
                <button class="btn btn-secondary" onclick="changeQuantity('${item.id}', 1)">
                    <i class="fas fa-plus"></i>
                </button>
                <button class="btn btn-danger" onclick="removeFromCart('${item.id}')">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;

        itemsContainer.appendChild(itemEl);
    });

    totalEl.textContent = `$${total.toFixed(2)}`;
}

// Ensure cart changes are persisted when quantities or removals occur
function _onCartChanged() {
    // Persist
    saveCart();

    // Compute total directly and update the badge so we never rely solely on external references
    try {
        const totalItems = (cart || []).reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
        if (cartCount) {
            cartCount.textContent = totalItems;
            cartCount.style.display = totalItems > 0 ? 'flex' : 'none';
        }
    } catch (e) {
        console.error('Error updating cart badge directly', e);
    }

    // Also call global updater if present (keeps compatibility)
    try {
        if (typeof window !== 'undefined' && typeof window.updateCartCount === 'function') {
            // If a global updater exists, call it (it's safe)
            window.updateCartCount();
        }
    } catch (e) {
        console.debug('updateCartCount not callable or failed:', e);
    }
}

function changeQuantity(productId, delta) {
    const pid = String(productId);
    const item = cart.find(i => String(i.id) === pid);
    if (!item) return;
    item.quantity += delta;
    if (item.quantity <= 0) {
        // Remove item from cart if quantity is zero or less
        cart = cart.filter(i => String(i.id) !== pid);
    }
    _onCartChanged();
    // Re-render cart items
    if (cartModal && cartModal.style.display === 'block') renderCartItems();
}

// Remove item from cart
function removeFromCart(productId) {
    const pid = String(productId);
    cart = cart.filter(i => String(i.id) !== pid);
    _onCartChanged();
    // Re-render cart items
    if (cartModal && cartModal.style.display === 'block') renderCartItems();
}

// Utility Functions
function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<p class="loading">Loading...</p>';
    }
}

function showError(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `<p class="error">${message}</p>`;
    }
}

function showAlert(message, type = 'info') {
    const alertBox = document.createElement('div');
    alertBox.className = `alert alert-${type}`;
    alertBox.textContent = message;

    document.body.appendChild(alertBox);

    // Auto-remove alert after 3 seconds
    setTimeout(() => {
        alertBox.classList.add('fade');
        setTimeout(() => {
            if (alertBox.parentNode) alertBox.parentNode.removeChild(alertBox);
        }, 300);
    }, 3000);
}

function scrollToSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
        section.scrollIntoView({ behavior: 'smooth' });
    }
}

