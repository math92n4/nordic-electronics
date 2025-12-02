import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';

export function Navbar({ onCartClick, onAuthClick }) {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const { currentUser, logout } = useAuth();
    const { getTotalItems } = useCart();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    const handleLinkClick = () => {
        setIsMenuOpen(false);
    };

    return (
        <nav className="navbar">
            <div className="nav-container">
                <Link to="/" className="nav-logo" onClick={handleLinkClick}>
                    <h2><i className="fas fa-bolt"></i> Nordic Electronics</h2>
                </Link>
                <div className={`nav-menu ${isMenuOpen ? 'active' : ''}`} id="nav-menu">
                    <Link to="/" className="nav-link" onClick={handleLinkClick}>Home</Link>
                    <Link to="/products" className="nav-link" onClick={handleLinkClick}>Products</Link>
                    <Link to="/categories" className="nav-link" onClick={handleLinkClick}>Categories</Link>
                    <Link to="/brands" className="nav-link" onClick={handleLinkClick}>Brands</Link>
                    {currentUser && (
                        <Link to="/orders" className="nav-link" onClick={handleLinkClick}>My Orders</Link>
                    )}
                    <button 
                        type="button"
                        className="nav-link" 
                        style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit', font: 'inherit' }}
                        onClick={(e) => {
                            e.preventDefault();
                            setIsMenuOpen(false);
                            if (currentUser) {
                                handleLogout();
                            } else {
                                onAuthClick();
                            }
                        }}
                    >
                        {currentUser ? 'Logout' : 'Login'}
                    </button>
                </div>
                <div className="cart-icon" id="cart-icon" onClick={onCartClick}>
                    <i className="fas fa-shopping-cart"></i>
                    {getTotalItems() > 0 && (
                        <span className="cart-count" id="cart-count">{getTotalItems()}</span>
                    )}
                </div>
                <div 
                    className="hamburger" 
                    id="hamburger"
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                >
                    <span className="bar"></span>
                    <span className="bar"></span>
                    <span className="bar"></span>
                </div>
            </div>
        </nav>
    );
}

