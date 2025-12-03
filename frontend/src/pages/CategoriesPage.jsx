import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { categoriesAPI } from '../api';

export function CategoriesPage() {
    const navigate = useNavigate();
    const { data: categories = [], isLoading } = useQuery({
        queryKey: ['categories'],
        queryFn: () => categoriesAPI.getAll(),
    });

    const filterByCategory = (categoryId) => {
        // Navigate with query parameter
        navigate(`/products?category=${categoryId}`);
        // Also dispatch event for immediate update if already on products page
        window.dispatchEvent(new CustomEvent('filterByCategory', { detail: { categoryId } }));
    };

    if (isLoading) {
        return (
            <div className="section">
                <div className="container">
                    <h2 className="section-title">Categories</h2>
                    <p className="loading">Loading...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="section">
            <div className="container">
                <h2 className="section-title">Categories</h2>
                <div id="categories-grid" className="categories-grid">
                    {categories.length === 0 ? (
                        <p className="no-results">No categories found</p>
                    ) : (
                        categories.map((category, index) => {
                            const categoryId = category.categoryId || category.id;
                            return (
                                <div 
                                    key={categoryId || `category-${index}`} 
                                    className="category-card" 
                                    onClick={() => filterByCategory(categoryId)}
                                >
                                <div className="category-icon">
                                    <i className="fas fa-tag"></i>
                                </div>
                                <h3 className="category-name">{category.name || 'Unnamed Category'}</h3>
                            </div>
                            );
                        })
                    )}
                </div>
            </div>
        </div>
    );
}

