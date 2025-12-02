import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { ordersAPI } from '../api';
import { useAuth } from '../hooks/useAuth';
import { showAlert } from '../utils/alerts';
import { OrderDetailsModal } from '../components/OrderDetailsModal';

export function OrdersPage() {
    const navigate = useNavigate();
    const { currentUser } = useAuth();
    const [selectedOrder, setSelectedOrder] = useState(null);

    const { data: orders = [], isLoading } = useQuery({
        queryKey: ['orders', currentUser?.userId || currentUser?.user?.userId],
        queryFn: () => {
            const userId = currentUser?.userId || currentUser?.user?.userId;
            if (!userId) throw new Error('User ID not found');
            return ordersAPI.getByUser(userId);
        },
        enabled: !!currentUser && !!(currentUser?.userId || currentUser?.user?.userId),
    });

    const viewOrderDetails = (orderId) => {
        const order = orders.find(o => {
            const oId = o.orderId || o.id;
            return String(oId) === String(orderId);
        });
        if (order) {
            setSelectedOrder(order);
        } else {
            showAlert('Order not found', 'error');
        }
    };

    const reorderItems = (orderId) => {
        showAlert('Items added to cart!', 'success');
    };

    if (!currentUser) {
        return (
            <div className="section">
                <div className="container">
                    <h2 className="section-title">My Orders</h2>
                    <p className="error">Please login to view orders</p>
                </div>
            </div>
        );
    }

    if (isLoading) {
        return (
            <div className="section">
                <div className="container">
                    <h2 className="section-title">My Orders</h2>
                    <p className="loading">Loading...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="section">
            <div className="container">
                <h2 className="section-title">My Orders</h2>
                <div id="orders-grid" className="orders-grid">
                    {orders.length === 0 ? (
                        <div className="no-orders">
                            <p>You haven't placed any orders yet.</p>
                            <button 
                                className="btn btn-primary" 
                                onClick={() => navigate('/products')}
                            >
                                Start Shopping
                            </button>
                        </div>
                    ) : (
                        orders.map(order => {
                            const orderId = order.orderId || order.id || '';
                            const orderDate = order.orderDate 
                                ? new Date(order.orderDate).toLocaleDateString() 
                                : (order.createdAt 
                                    ? new Date(order.createdAt).toLocaleDateString() 
                                    : (order.created_at 
                                        ? new Date(order.created_at).toLocaleDateString() 
                                        : 'N/A'));
                            const status = order.status || order.orderStatus || 'UNKNOWN';
                            
                            let orderItems = [];
                            let totalItems = 0;
                            let totalAmount = 0;
                            
                            if (order.totalAmount !== undefined && order.totalAmount !== null) {
                                totalAmount = parseFloat(order.totalAmount) || 0;
                            }
                            
                            if (order.orderProducts && Array.isArray(order.orderProducts)) {
                                orderItems = order.orderProducts;
                                totalItems = orderItems.reduce((sum, item) => sum + (item.quantity || 0), 0);
                                if (totalAmount === 0) {
                                    totalAmount = orderItems.reduce((sum, item) => sum + (parseFloat(item.totalPrice) || 0), 0);
                                }
                            } else if (order.cart && Array.isArray(order.cart)) {
                                orderItems = order.cart;
                                totalItems = orderItems.reduce((sum, item) => sum + (item.quantity || 0), 0);
                                if (totalAmount === 0) {
                                    totalAmount = orderItems.reduce((sum, item) => sum + (parseFloat(item.price) || 0) * (item.quantity || 0), 0);
                                }
                            }

                            const orderIdStr = orderId ? String(orderId) : 'N/A';
                            const orderIdDisplay = orderIdStr.length > 8 ? orderIdStr.substring(0, 8) : orderIdStr;

                            return (
                                <div key={orderId} className="order-card">
                                    <div className="order-header">
                                        <div className="order-info">
                                            <h3>Order #{orderIdDisplay}</h3>
                                            <p className="order-date">{orderDate}</p>
                                            <span className={`order-status status-${status.toLowerCase()}`}>{status}</span>
                                        </div>
                                        <div className="order-summary">
                                            <p className="order-total">${totalAmount.toFixed(2)}</p>
                                            <p className="order-items">{totalItems} items</p>
                                        </div>
                                    </div>
                                    <div className="order-items-list">
                                        {orderItems.length > 0 ? orderItems.map((item, idx) => {
                                            const productName = item.product 
                                                ? (item.product.name || 'Unknown Product') 
                                                : (item.name || 'Unknown Product');
                                            const quantity = item.quantity || 0;
                                            const price = item.totalPrice 
                                                ? parseFloat(item.totalPrice) 
                                                : (item.unitPrice 
                                                    ? parseFloat(item.unitPrice) * quantity 
                                                    : (item.price 
                                                        ? parseFloat(item.price) * quantity 
                                                        : 0));
                                            
                                            return (
                                                <div key={idx} className="order-item">
                                                    <span className="item-name">{productName}</span>
                                                    <span className="item-quantity">Qty: {quantity}</span>
                                                    <span className="item-price">${price.toFixed(2)}</span>
                                                </div>
                                            );
                                        }) : <p className="no-items">No items in this order</p>}
                                    </div>
                                    <div className="order-actions">
                                        <button 
                                            className="btn btn-secondary" 
                                            onClick={() => viewOrderDetails(orderId)}
                                        >
                                            <i className="fas fa-eye"></i> View Details
                                        </button>
                                        {(status.toUpperCase() === 'PENDING' || status.toUpperCase() === 'pending') && (
                                            <button 
                                                className="btn btn-primary" 
                                                onClick={() => reorderItems(orderId)}
                                            >
                                                <i className="fas fa-redo"></i> Reorder
                                            </button>
                                        )}
                                    </div>
                                </div>
                            );
                        })
                    )}
                </div>
            </div>
            
            <OrderDetailsModal
                isOpen={!!selectedOrder}
                onClose={() => setSelectedOrder(null)}
                order={selectedOrder}
            />
        </div>
    );
}

