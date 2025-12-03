import { useState, useEffect } from 'react';
import { useCart } from '../hooks/useCart';
import { useAuth } from '../hooks/useAuth';
import { stripeAPI, couponsAPI } from '../api';
import { showAlert } from '../utils/alerts';

export function CartModal({ isOpen, onClose }) {
    const { cart, removeFromCart, changeQuantity, clearCart, getTotalPrice } = useCart();
    const { currentUser } = useAuth();
    const [isCheckingOut, setIsCheckingOut] = useState(false);
    const [isAddressOpen, setIsAddressOpen] = useState(false);
    const [isCouponOpen, setIsCouponOpen] = useState(false);
    const [couponCode, setCouponCode] = useState('');
    const [couponError, setCouponError] = useState('');
    const [appliedCoupon, setAppliedCoupon] = useState(null);
    const [couponDiscount, setCouponDiscount] = useState(0);
    const [isValidatingCoupon, setIsValidatingCoupon] = useState(false);
    const [address, setAddress] = useState({
        street: '',
        streetNumber: '',
        zip: '',
        city: '',
    });
    const [addressErrors, setAddressErrors] = useState({
        street: '',
        streetNumber: '',
        zip: '',
        city: '',
    });
    const [touched, setTouched] = useState({
        street: false,
        streetNumber: false,
        zip: false,
        city: false,
    });

    // Handle body overflow when modal is open
    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'auto';
            // Reset form state when modal closes
            setAddress({
                street: '',
                streetNumber: '',
                zip: '',
                city: '',
            });
            setAddressErrors({
                street: '',
                streetNumber: '',
                zip: '',
                city: '',
            });
            setTouched({
                street: false,
                streetNumber: false,
                zip: false,
                city: false,
            });
            setIsAddressOpen(false);
            setIsCouponOpen(false);
            setCouponCode('');
            setCouponError('');
            setAppliedCoupon(null);
            setCouponDiscount(0);
        }
        return () => {
            document.body.style.overflow = 'auto';
        };
    }, [isOpen]);

    if (!isOpen) return null;

    // Validate address fields
    const validateAddress = () => {
        const errors = {
            street: '',
            streetNumber: '',
            zip: '',
            city: '',
        };

        if (!address.street || address.street.trim() === '') {
            errors.street = 'Street is required';
        }

        if (!address.streetNumber || address.streetNumber.trim() === '') {
            errors.streetNumber = 'Street number is required';
        }

        if (!address.zip || address.zip.trim() === '') {
            errors.zip = 'ZIP code is required';
        } else if (!/^\d{4,10}$/.test(address.zip.trim())) {
            errors.zip = 'ZIP code must be 4-10 digits';
        }

        if (!address.city || address.city.trim() === '') {
            errors.city = 'City is required';
        }

        setAddressErrors(errors);
        return !Object.values(errors).some(error => error !== '');
    };

    // Validate individual field on blur
    const validateField = (fieldName, value) => {
        const errors = { ...addressErrors };
        
        switch (fieldName) {
            case 'street':
                errors.street = !value || value.trim() === '' ? 'Street is required' : '';
                break;
            case 'streetNumber':
                errors.streetNumber = !value || value.trim() === '' ? 'Street number is required' : '';
                break;
            case 'zip':
                if (!value || value.trim() === '') {
                    errors.zip = 'ZIP code is required';
                } else if (!/^\d{4,10}$/.test(value.trim())) {
                    errors.zip = 'ZIP code must be 4-10 digits';
                } else {
                    errors.zip = '';
                }
                break;
            case 'city':
                errors.city = !value || value.trim() === '' ? 'City is required' : '';
                break;
        }
        
        setAddressErrors(errors);
    };

    const handleAddressChange = (field, value) => {
        setAddress({ ...address, [field]: value });
        // Clear error when user starts typing
        if (touched[field] && addressErrors[field]) {
            validateField(field, value);
        }
    };

    const handleAddressBlur = (field) => {
        setTouched({ ...touched, [field]: true });
        validateField(field, address[field]);
    };

    const toggleAddressAccordion = () => {
        setIsAddressOpen(!isAddressOpen);
    };

    const toggleCouponAccordion = () => {
        setIsCouponOpen(!isCouponOpen);
    };

    const handleApplyCoupon = async () => {
        const code = couponCode.trim().toUpperCase();
        if (!code) {
            setCouponError('Please enter a coupon code');
            return;
        }
        
        // Clear previous error
        setCouponError('');
        setIsValidatingCoupon(true);
        
        try {
            // Calculate subtotal from cart
            const subtotal = getTotalPrice();
            
            if (subtotal <= 0) {
                setCouponError('Cart must have items to apply a coupon');
                return;
            }
            
            // Validate coupon with backend
            const discount = await couponsAPI.validate(code, subtotal);
            
            // Discount is returned as a number (BigDecimal from backend)
            const discountAmount = parseFloat(discount) || 0;
            
            if (discountAmount > 0) {
                setAppliedCoupon(code);
                setCouponDiscount(discountAmount);
                showAlert(`Coupon ${code} applied! Discount: $${discountAmount.toFixed(2)}`, 'success');
            } else {
                setCouponError('Invalid coupon code or does not meet minimum order requirements');
                setAppliedCoupon(null);
                setCouponDiscount(0);
            }
        } catch (error) {
            console.error('Coupon validation error:', error);
            // Extract error message - could be a string or object
            let errorMessage = 'Invalid coupon code or does not meet minimum order requirements';
            if (error.message) {
                errorMessage = error.message;
            } else if (typeof error === 'string') {
                errorMessage = error;
            }
            
            setCouponError(errorMessage);
            setAppliedCoupon(null);
            setCouponDiscount(0);
            showAlert(errorMessage, 'error');
        } finally {
            setIsValidatingCoupon(false);
        }
    };

    const handleRemoveCoupon = () => {
        setAppliedCoupon(null);
        setCouponCode('');
        setCouponError('');
        setCouponDiscount(0);
        showAlert('Coupon removed', 'info');
    };

    const handleCheckout = async () => {
        if (!currentUser) {
            showAlert('Please login to proceed to checkout', 'error');
            return;
        }

        if (cart.length === 0) {
            showAlert('Your cart is empty', 'error');
            return;
        }

        // Validate address before checkout
        if (!validateAddress()) {
            showAlert('Please fill in all required address fields correctly', 'error');
            setIsAddressOpen(true); // Open accordion to show errors
            // Mark all fields as touched to show errors
            setTouched({
                street: true,
                streetNumber: true,
                zip: true,
                city: true,
            });
            return;
        }

        const addressData = {
            street: address.street.trim(),
            streetNumber: address.streetNumber.trim(),
            zip: address.zip.trim(),
            city: address.city.trim(),
        };

        setIsCheckingOut(true);
        showAlert('Creating checkout session...', 'info');

        try {
            const successUrl = window.location.origin + '/?checkout=success';
            const cancelUrl = window.location.origin + '/?checkout=cancel';

            const data = await stripeAPI.createCheckout(cart, addressData, successUrl, cancelUrl, appliedCoupon);

            if (data.url) {
                clearCart();
                setAddress({ street: '', streetNumber: '', zip: '', city: '' });
                setCouponCode('');
                setAppliedCoupon(null);
                setCouponError('');
                setCouponDiscount(0);
                onClose();
                window.location.href = data.url;
            } else {
                showAlert('No checkout URL received', 'error');
            }
        } catch (error) {
            console.error('Checkout error:', error);
            // Extract error message properly
            let errorMessage = 'Failed to initiate checkout. Please try again.';
            if (error && error.message) {
                errorMessage = error.message;
            } else if (typeof error === 'string') {
                errorMessage = error;
            } else if (error && typeof error === 'object' && error.error) {
                errorMessage = error.error;
            }
            
            // If error mentions product not found, suggest clearing cart
            if (errorMessage.toLowerCase().includes('product not found')) {
                errorMessage += '. Some items in your cart may no longer be available. Please remove them and try again.';
            }
            
            showAlert(errorMessage, 'error');
        } finally {
            setIsCheckingOut(false);
        }
    };


    const scrollToSection = (sectionId) => {
        const section = document.getElementById(sectionId);
        if (section) {
            section.scrollIntoView({ behavior: 'smooth' });
        }
    };

    return (
        <div 
            id="cart-modal" 
            className="modal" 
            style={{ display: isOpen ? 'flex' : 'none' }}
            onClick={(e) => e.target.id === 'cart-modal' && onClose()}
        >
            <div className="modal-content cart-modal-content">
                <span className="close" onClick={onClose}>&times;</span>
                <div className="cart-container">
                    <h3 className="cart-title"><i className="fas fa-shopping-cart"></i> Your Cart</h3>
                    <div className="cart-items" id="cart-items">
                        {cart.length === 0 ? (
                            <p className="no-results">Your cart is empty</p>
                        ) : (
                            cart.map(item => {
                                // Debug: log the item to see what we're working with
                                if (process.env.NODE_ENV === 'development') {
                                    console.log('Cart item:', item);
                                }
                                
                                const price = Number(item.price) || 0;
                                const name = item.name || 'Unnamed Product';
                                const quantity = Number(item.quantity) || 0;
                                const subtotal = price * quantity;
                                
                                // Debug: log price calculation
                                if (process.env.NODE_ENV === 'development' && price === 0) {
                                    console.warn('Item has price 0:', item);
                                }

                                return (
                                    <div key={item.id} className="cart-item" data-product-id={item.id}>
                                        <div className="cart-item-info">
                                            <h4 className="cart-item-name">{name}</h4>
                                            <div className="cart-item-details">
                                                <span className="cart-item-price">${price.toFixed(2)}</span>
                                                <span className="cart-item-quantity">Qty: {quantity}</span>
                                            </div>
                                        </div>
                                        <div className="cart-item-actions">
                                            <button 
                                                className="btn btn-secondary" 
                                                onClick={() => changeQuantity(item.id, -1)}
                                            >
                                                <i className="fas fa-minus"></i>
                                            </button>
                                            <button 
                                                className="btn btn-secondary" 
                                                onClick={() => changeQuantity(item.id, 1)}
                                            >
                                                <i className="fas fa-plus"></i>
                                            </button>
                                            <button 
                                                className="btn btn-danger" 
                                                onClick={() => removeFromCart(item.id)}
                                            >
                                                <i className="fas fa-trash"></i>
                                            </button>
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </div>
                    {currentUser && cart.length > 0 && (
                        <div className="cart-address-section" id="cart-address-section">
                            <div 
                                className="address-accordion-header"
                                onClick={toggleAddressAccordion}
                            >
                                <h4 className="address-title">
                                    <i className="fas fa-map-marker-alt"></i> Shipping Address
                                </h4>
                                <i className={`fas fa-chevron-${isAddressOpen ? 'up' : 'down'} address-accordion-icon`}></i>
                            </div>
                            <div className={`address-accordion-content ${isAddressOpen ? 'open' : ''}`}>
                            <div className="address-form">
                                <div className="form-group">
                                        <label htmlFor="address-street" className="form-label">
                                            Street <span className="required">*</span>
                                        </label>
                                    <input 
                                        type="text" 
                                        id="address-street" 
                                            className={`form-input ${touched.street && addressErrors.street ? 'error' : ''}`}
                                            placeholder="Street name" 
                                        required
                                        value={address.street}
                                            onChange={(e) => handleAddressChange('street', e.target.value)}
                                            onBlur={() => handleAddressBlur('street')}
                                    />
                                        {touched.street && addressErrors.street && (
                                            <span className="error-message">{addressErrors.street}</span>
                                        )}
                                </div>
                                <div className="form-group">
                                        <label htmlFor="address-street-number" className="form-label">
                                            Street Number <span className="required">*</span>
                                        </label>
                                    <input 
                                        type="text" 
                                        id="address-street-number" 
                                            className={`form-input ${touched.streetNumber && addressErrors.streetNumber ? 'error' : ''}`}
                                            placeholder="Street number" 
                                        required
                                        value={address.streetNumber}
                                            onChange={(e) => handleAddressChange('streetNumber', e.target.value)}
                                            onBlur={() => handleAddressBlur('streetNumber')}
                                    />
                                        {touched.streetNumber && addressErrors.streetNumber && (
                                            <span className="error-message">{addressErrors.streetNumber}</span>
                                        )}
                                </div>
                                <div className="form-group-row">
                                    <div className="form-group">
                                            <label htmlFor="address-zip" className="form-label">
                                                ZIP Code <span className="required">*</span>
                                            </label>
                                        <input 
                                            type="text" 
                                            id="address-zip" 
                                                className={`form-input ${touched.zip && addressErrors.zip ? 'error' : ''}`}
                                            placeholder="ZIP Code" 
                                            required
                                            value={address.zip}
                                                onChange={(e) => handleAddressChange('zip', e.target.value)}
                                                onBlur={() => handleAddressBlur('zip')}
                                        />
                                            {touched.zip && addressErrors.zip && (
                                                <span className="error-message">{addressErrors.zip}</span>
                                            )}
                                    </div>
                                    <div className="form-group">
                                            <label htmlFor="address-city" className="form-label">
                                                City <span className="required">*</span>
                                            </label>
                                        <input 
                                            type="text" 
                                            id="address-city" 
                                                className={`form-input ${touched.city && addressErrors.city ? 'error' : ''}`}
                                            placeholder="City" 
                                            required
                                            value={address.city}
                                                onChange={(e) => handleAddressChange('city', e.target.value)}
                                                onBlur={() => handleAddressBlur('city')}
                                        />
                                            {touched.city && addressErrors.city && (
                                                <span className="error-message">{addressErrors.city}</span>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                    {currentUser && cart.length > 0 && (
                        <div className="cart-coupon-section" id="cart-coupon-section">
                            <div 
                                className="coupon-accordion-header"
                                onClick={toggleCouponAccordion}
                            >
                                <h4 className="coupon-title">
                                    <i className="fas fa-tag"></i> Coupon Code
                                    {appliedCoupon && (
                                        <span className="coupon-applied-badge">
                                            <i className="fas fa-check-circle"></i> {appliedCoupon}
                                        </span>
                                    )}
                                </h4>
                                <i className={`fas fa-chevron-${isCouponOpen ? 'up' : 'down'} coupon-accordion-icon`}></i>
                            </div>
                            <div className={`coupon-accordion-content ${isCouponOpen ? 'open' : ''}`}>
                                <div className="coupon-form">
                                    {appliedCoupon ? (
                                        <div className="coupon-applied">
                                            <div className="coupon-applied-info">
                                                <i className="fas fa-check-circle"></i>
                                                <div className="coupon-applied-details">
                                                    <span>Coupon <strong>{appliedCoupon}</strong> is applied</span>
                                                    <span className="coupon-discount-amount">-${couponDiscount.toFixed(2)} discount</span>
                                                </div>
                                            </div>
                                            <button 
                                                className="btn btn-secondary btn-sm"
                                                onClick={handleRemoveCoupon}
                                            >
                                                Remove
                                            </button>
                                        </div>
                                    ) : (
                                        <div className="coupon-input-group">
                                            <input 
                                                type="text" 
                                                id="coupon-code" 
                                                className={`form-input ${couponError ? 'error' : ''}`}
                                                placeholder="Enter coupon code" 
                                                value={couponCode}
                                                onChange={(e) => {
                                                    setCouponCode(e.target.value.toUpperCase());
                                                    setCouponError('');
                                                }}
                                                onKeyPress={(e) => {
                                                    if (e.key === 'Enter') {
                                                        handleApplyCoupon();
                                                    }
                                                }}
                                            />
                                            <button 
                                                className="btn btn-primary"
                                                onClick={handleApplyCoupon}
                                                disabled={!couponCode.trim() || isValidatingCoupon}
                                            >
                                                {isValidatingCoupon ? 'Validating...' : 'Apply'}
                                            </button>
                                        </div>
                                    )}
                                    {couponError && (
                                        <span className="error-message">{couponError}</span>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                    <div className="cart-summary">
                        <div className="cart-subtotal" id="cart-subtotal">
                            <span className="cart-summary-label">Subtotal:</span>
                            <span className="cart-summary-value">${(getTotalPrice() || 0).toFixed(2)}</span>
                        </div>
                        {couponDiscount > 0 && (
                            <div className="cart-discount">
                                <span className="cart-summary-label">Discount:</span>
                                <span className="cart-summary-value discount">-${couponDiscount.toFixed(2)}</span>
                            </div>
                        )}
                        <div className="cart-total" id="cart-total">
                            <span className="cart-total-label">Total:</span>
                            <span className="cart-total-amount">${((getTotalPrice() || 0) - couponDiscount).toFixed(2)}</span>
                        </div>
                        <div className="cart-actions">
                            <button 
                                className="btn btn-secondary" 
                                onClick={() => { onClose(); scrollToSection('products'); }}
                            >
                                Continue Shopping
                            </button>
                            <button 
                                className="btn btn-primary" 
                                id="checkout-btn"
                                onClick={handleCheckout}
                                disabled={isCheckingOut || cart.length === 0}
                            >
                                {isCheckingOut ? 'Processing...' : 'Checkout'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

