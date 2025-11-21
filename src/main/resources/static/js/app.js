// API Configuration
const API_BASE_URL = '/api';
const API_ENDPOINTS = {
    AUTH: `${API_BASE_URL}/postgresql/auth`,
    PRODUCTS: `${API_BASE_URL}/postgresql/products`,
    CATEGORIES: `${API_BASE_URL}/postgresql/categories`,
    BRANDS: `${API_BASE_URL}/postgresql/brands`,
    WAREHOUSES: `${API_BASE_URL}/postgresql/warehouses`,
    REVIEWS: `${API_BASE_URL}/postgresql/reviews`
};

// Global State
let currentUser = null;
let products = [];
let categories = [];
let brands = [];
let cart = [];

// DOM Elements
const authModal = document.getElementById('auth-modal');
const authLink = document.getElementById('auth-link');
const hamburger = document.getElementById('hamburger');
const navMenu = document.getElementById('nav-menu');
const cartCount = document.getElementById('cart-count');

// Initialize App
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
});

// App Initialization
function initializeApp() {
    checkAuthStatus();
    loadProducts();
    loadCategories();
    loadBrands();
    updateCartCount();
}

// Event Listeners Setup
function setupEventListeners() {
    // Mobile Navigation
    if (hamburger) {
        hamburger.addEventListener('click', () => {
            navMenu.classList.toggle('active');
        });
    }

    // Auth Modal
    if (authLink) {
        authLink.addEventListener('click', (e) => {
            e.preventDefault();
            if (currentUser) {
                logout();
            } else {
                showAuthModal();
            }
        });
    }

    // Close modal
    const closeButton = document.querySelector('.close');
    if (closeButton) {
        closeButton.addEventListener('click', hideAuthModal);
    }

    window.addEventListener('click', (e) => {
        if (e.target === authModal) {
            hideAuthModal();
        }
    });

    // Auth Forms
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');

    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // Search and Filters
    const searchInput = document.getElementById('search-input');
    const categoryFilter = document.getElementById('category-filter');
    const brandFilter = document.getElementById('brand-filter');

    if (searchInput) {
        searchInput.addEventListener('input', filterProducts);
    }
    if (categoryFilter) {
        categoryFilter.addEventListener('change', filterProducts);
    }
    if (brandFilter) {
        brandFilter.addEventListener('change', filterProducts);
    }

    // Smooth scrolling for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// Authentication Functions
function checkAuthStatus() {
    // Check if user is logged in (you might want to implement session checking)
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
        currentUser = JSON.parse(storedUser);
        updateAuthUI();
    }
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

        if (response.ok) {
            // Extract user data from response if it exists, otherwise use the full response
            currentUser = responseData.user || responseData;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
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
    currentUser = null;
    localStorage.removeItem('currentUser');
    updateAuthUI();
    cart = [];
    updateCartCount();
    showAlert('Logged out successfully', 'success');
}

function updateAuthUI() {
    if (currentUser) {
        // Handle different user data structures from login/register responses
        const firstName = currentUser.firstName || (currentUser.user && currentUser.user.firstName);
        const email = currentUser.email || (currentUser.user && currentUser.user.email);

        authLink.textContent = 'Logout';
        authLink.title = `Logged in as ${firstName || email}`;
    } else {
        authLink.textContent = 'Login';
        authLink.title = 'Click to login';
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

    grid.innerHTML = productsToShow.map(product => `
        <div class="product-card" data-product-id="${product.id}">
            <div class="product-image">
                <i class="fas fa-mobile-alt"></i>
            </div>
            <h3 class="product-title">${product.name || 'Unnamed Product'}</h3>
            <p class="product-description">${product.description || 'No description available'}</p>
            <div class="product-price">$${product.price || '0.00'}</div>
            <div class="product-actions">
                <button class="btn btn-primary" onclick="addToCart('${product.id}')">
                    <i class="fas fa-cart-plus"></i> Add to Cart
                </button>
                <button class="btn btn-secondary" onclick="viewProduct('${product.id}')">
                    <i class="fas fa-eye"></i> View
                </button>
            </div>
        </div>
    `).join('');
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
    if (!currentUser) {
        showAlert('Please login to add items to cart', 'error');
        showAuthModal();
        return;
    }

    const product = products.find(p => p.id === productId);
    if (product) {
        const existingItem = cart.find(item => item.id === productId);
        if (existingItem) {
            existingItem.quantity += 1;
        } else {
            cart.push({ ...product, quantity: 1 });
        }
        updateCartCount();
        showAlert(`${product.name} added to cart`, 'success');
    }
}

function updateCartCount() {
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    if (cartCount) {
        cartCount.textContent = totalItems;
    }
}

function viewProduct(productId) {
    const product = products.find(p => p.id === productId);
    if (product) {
        alert(`Product: ${product.name}\nPrice: $${product.price}\nDescription: ${product.description || 'No description available'}`);
    }
}

// Utility Functions
function scrollToSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
        section.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

function showLoading(elementId) {
    const element = document.getElementById(elementId);
    element.innerHTML = `
        <div class="loading">
            <div class="spinner"></div>
        </div>
    `;
}

function showError(elementId, message) {
    const element = document.getElementById(elementId);
    element.innerHTML = `
        <div class="error">
            <p>${message}</p>
            <button onclick="location.reload()">Retry</button>
        </div>
    `;
}

function showAlert(message, type = 'success') {
    // Remove existing alerts
    const existingAlert = document.querySelector('.alert');
    if (existingAlert) {
        existingAlert.remove();
    }

    // Create new alert
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;
    alert.style.position = 'fixed';
    alert.style.top = '100px';
    alert.style.right = '20px';
    alert.style.zIndex = '9999';
    alert.style.maxWidth = '300px';

    document.body.appendChild(alert);

    // Auto remove after 3 seconds
    setTimeout(() => {
        if (alert.parentNode) {
            alert.remove();
        }
    }, 3000);
}

// Global functions for HTML onclick handlers
window.showTab = showTab;
window.addToCart = addToCart;
window.viewProduct = viewProduct;
window.filterByCategory = filterByCategory;
window.filterByBrand = filterByBrand;
window.scrollToSection = scrollToSection;
