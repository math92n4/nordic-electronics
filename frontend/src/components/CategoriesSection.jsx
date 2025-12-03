import { useQuery } from '@tanstack/react-query';
import { categoriesAPI } from '../api';

export function CategoriesSection() {
    const { data: categories = [], isLoading } = useQuery({
        queryKey: ['categories'],
        queryFn: () => categoriesAPI.getAll(),
    });

    const filterByCategory = (categoryId) => {
        // Dispatch a custom event that ProductsSection can listen to
        window.dispatchEvent(new CustomEvent('filterByCategory', { detail: { categoryId } }));
        scrollToSection('products');
    };

    const scrollToSection = (sectionId) => {
        const section = document.getElementById(sectionId);
        if (section) {
            section.scrollIntoView({ behavior: 'smooth' });
        }
    };

    if (isLoading) {
        return (
            <section id="categories" className="section">
                <div className="container">
                    <h2 className="section-title">Categories</h2>
                    <p className="loading">Loading...</p>
                </div>
            </section>
        );
    }

    return (
        <section id="categories" className="section">
            <div className="container">
                <h2 className="section-title">Categories</h2>
                <div id="categories-grid" className="categories-grid">
                    {categories.length === 0 ? (
                        <p className="no-results">No categories found</p>
                    ) : (
                        categories.map((category, index) => (
                            <div 
                                key={category.id || `category-${index}`} 
                                className="category-card" 
                                onClick={() => filterByCategory(category.id)}
                            >
                                <div className="category-icon">
                                    <i className="fas fa-tag"></i>
                                </div>
                                <h3 className="category-name">{category.name || 'Unnamed Category'}</h3>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </section>
    );
}

