import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CartProvider } from './contexts/CartContext';
import { Layout } from './components/Layout';
import { HomePage } from './pages/HomePage';
import { ProductsPage } from './pages/ProductsPage';
import { CategoriesPage } from './pages/CategoriesPage';
import { BrandsPage } from './pages/BrandsPage';
import { OrdersPage } from './pages/OrdersPage';

// Create a query client
const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            retry: 1,
            staleTime: 5 * 60 * 1000, // 5 minutes
        },
    },
});

export default function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <CartProvider>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<Layout />}>
                            <Route index element={<HomePage />} />
                            <Route path="products" element={<ProductsPage />} />
                            <Route path="categories" element={<CategoriesPage />} />
                            <Route path="brands" element={<BrandsPage />} />
                            <Route path="orders" element={<OrdersPage />} />
                        </Route>
                    </Routes>
                </BrowserRouter>
            </CartProvider>
        </QueryClientProvider>
    );
}

