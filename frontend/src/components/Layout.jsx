import { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';
import { AuthModal } from './AuthModal';
import { CartModal } from './CartModal';
import { showAlert } from '../utils/alerts';

export function Layout() {
    const [authModalOpen, setAuthModalOpen] = useState(false);
    const [cartModalOpen, setCartModalOpen] = useState(false);

    // Handle escape key to close modals
    useEffect(() => {
        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                setAuthModalOpen(false);
                setCartModalOpen(false);
            }
        };
        window.addEventListener('keydown', handleEscape);
        return () => window.removeEventListener('keydown', handleEscape);
    }, []);

    return (
        <>
            <Navbar 
                onCartClick={() => setCartModalOpen(true)}
                onAuthClick={() => setAuthModalOpen(true)}
            />
            
            <main>
                <Outlet />
            </main>

            <AuthModal 
                isOpen={authModalOpen}
                onClose={() => setAuthModalOpen(false)}
                onSuccess={showAlert}
            />

            <CartModal 
                isOpen={cartModalOpen}
                onClose={() => setCartModalOpen(false)}
            />

            <footer className="footer">
                <div className="container">
                    <div className="footer-content">
                        <div className="footer-section">
                            <h3><i className="fas fa-bolt"></i> Nordic Electronics</h3>
                            <p>Your trusted partner for electronic gadgets and technology.</p>
                        </div>
                        <div className="footer-section">
                            <h4>Quick Links</h4>
                            <ul>
                                <li><a href="/">Home</a></li>
                                <li><a href="/products">Products</a></li>
                                <li><a href="/categories">Categories</a></li>
                                <li><a href="/brands">Brands</a></li>
                            </ul>
                        </div>
                        <div className="footer-section">
                            <h4>Contact</h4>
                            <p><i className="fas fa-envelope"></i> info@nordicelectronics.com</p>
                            <p><i className="fas fa-phone"></i> +1 (555) 123-4567</p>
                        </div>
                    </div>
                    <div className="footer-bottom">
                        <p>&copy; 2025 Nordic Electronics. All rights reserved.</p>
                    </div>
                </div>
            </footer>
        </>
    );
}

