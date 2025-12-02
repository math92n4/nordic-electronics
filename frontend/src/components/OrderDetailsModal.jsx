import { useEffect } from 'react';

export function OrderDetailsModal({ isOpen, onClose, order }) {
    // Handle body overflow when modal is open
    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'auto';
        }
        return () => {
            document.body.style.overflow = 'auto';
        };
    }, [isOpen]);

    // Handle escape key
    useEffect(() => {
        if (!isOpen) return;
        
        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                onClose();
            }
        };
        
        window.addEventListener('keydown', handleEscape);
        return () => window.removeEventListener('keydown', handleEscape);
    }, [isOpen, onClose]);

    if (!isOpen || !order) return null;

    // Extract order data with fallbacks
    const orderId = order.orderId || order.id || 'N/A';
    const orderDate = order.orderDate 
        ? new Date(order.orderDate).toLocaleString() 
        : (order.createdAt 
            ? new Date(order.createdAt).toLocaleString() 
            : (order.created_at 
                ? new Date(order.created_at).toLocaleString() 
                : 'N/A'));
    const status = order.status || order.orderStatus || 'UNKNOWN';
    
    // Extract address
    const address = order.address || {};
    const addressString = address.street && address.streetNumber && address.zip && address.city
        ? `${address.street} ${address.streetNumber}, ${address.zip} ${address.city}`
        : 'No address provided';

    // Extract user info
    const user = order.user || {};
    const userName = user.firstName && user.lastName
        ? `${user.firstName} ${user.lastName}`
        : (user.email || 'Unknown User');

    // Extract order products
    const orderProducts = order.orderProducts || order.order_items || order.cart || [];
    
    // Calculate totals
    const subtotal = parseFloat(order.subtotal) || 0;
    const taxAmount = parseFloat(order.taxAmount) || 0;
    const shippingCost = parseFloat(order.shippingCost) || 0;
    const discountAmount = parseFloat(order.discountAmount) || 0;
    const totalAmount = parseFloat(order.totalAmount) || 0;

    return (
        <div 
            className="modal" 
            style={{ display: isOpen ? 'flex' : 'none' }}
            onClick={(e) => e.target.className === 'modal' && onClose()}
        >
            <div className="modal-content order-modal-content">
                <span className="close" onClick={onClose}>&times;</span>
                
                <div className="order-modal-header">
                    <h2 className="order-modal-title">
                        <i className="fas fa-receipt"></i> Order Details
                    </h2>
                    <div className="order-modal-id">Order #{String(orderId).substring(0, 8)}</div>
                </div>

                <div className="order-modal-body">
                    {/* Order Info Section */}
                    <div className="order-details-section">
                        <h3 className="order-details-section-title">
                            <i className="fas fa-info-circle"></i> Order Information
                        </h3>
                        <div className="order-details-grid">
                            <div className="order-detail-item">
                                <span className="order-detail-label">Order Date:</span>
                                <span className="order-detail-value">{orderDate}</span>
                            </div>
                            <div className="order-detail-item">
                                <span className="order-detail-label">Status:</span>
                                <span className={`order-detail-value order-status status-${status.toLowerCase()}`}>
                                    {status}
                                </span>
                            </div>
                            <div className="order-detail-item">
                                <span className="order-detail-label">Customer:</span>
                                <span className="order-detail-value">{userName}</span>
                            </div>
                            {user.email && (
                                <div className="order-detail-item">
                                    <span className="order-detail-label">Email:</span>
                                    <span className="order-detail-value">{user.email}</span>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Shipping Address Section */}
                    <div className="order-details-section">
                        <h3 className="order-details-section-title">
                            <i className="fas fa-map-marker-alt"></i> Shipping Address
                        </h3>
                        <div className="order-address">
                            <p>{addressString}</p>
                        </div>
                    </div>

                    {/* Order Items Section */}
                    <div className="order-details-section">
                        <h3 className="order-details-section-title">
                            <i className="fas fa-shopping-bag"></i> Order Items ({orderProducts.length})
                        </h3>
                        <div className="order-items-list">
                            {orderProducts.length > 0 ? (
                                orderProducts.map((item, idx) => {
                                    const product = item.product || {};
                                    const productName = product.name || item.name || 'Unknown Product';
                                    const quantity = item.quantity || 0;
                                    const unitPrice = parseFloat(item.unitPrice || item.price || 0);
                                    const totalPrice = parseFloat(item.totalPrice || (unitPrice * quantity) || 0);
                                    
                                    return (
                                        <div key={idx} className="order-item-detail">
                                            <div className="order-item-detail-info">
                                                <h4 className="order-item-detail-name">{productName}</h4>
                                                {product.description && (
                                                    <p className="order-item-detail-description">{product.description}</p>
                                                )}
                                                <div className="order-item-detail-meta">
                                                    <span className="order-item-detail-quantity">Quantity: {quantity}</span>
                                                    <span className="order-item-detail-unit-price">
                                                        ${unitPrice.toFixed(2)} each
                                                    </span>
                                                </div>
                                            </div>
                                            <div className="order-item-detail-price">
                                                ${totalPrice.toFixed(2)}
                                            </div>
                                        </div>
                                    );
                                })
                            ) : (
                                <p className="no-items">No items in this order</p>
                            )}
                        </div>
                    </div>

                    {/* Order Summary Section */}
                    <div className="order-details-section">
                        <h3 className="order-details-section-title">
                            <i className="fas fa-calculator"></i> Order Summary
                        </h3>
                        <div className="order-summary-details">
                            <div className="order-summary-row">
                                <span className="order-summary-label">Subtotal:</span>
                                <span className="order-summary-value">${subtotal.toFixed(2)}</span>
                            </div>
                            {discountAmount > 0 && (
                                <div className="order-summary-row discount">
                                    <span className="order-summary-label">Discount:</span>
                                    <span className="order-summary-value">-${discountAmount.toFixed(2)}</span>
                                </div>
                            )}
                            <div className="order-summary-row">
                                <span className="order-summary-label">Shipping:</span>
                                <span className="order-summary-value">${shippingCost.toFixed(2)}</span>
                            </div>
                            <div className="order-summary-row total">
                                <span className="order-summary-label">Total:</span>
                                <span className="order-summary-value">${totalAmount.toFixed(2)}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="order-modal-footer">
                    <button className="btn btn-primary" onClick={onClose}>
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
}

