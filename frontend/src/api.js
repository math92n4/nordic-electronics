// API Configuration
const API_BASE_URL = "/api";
export const API_ENDPOINTS = {
  AUTH: `${API_BASE_URL}/postgresql/auth`,
  PRODUCTS: `${API_BASE_URL}/postgresql/products`,
  CATEGORIES: `${API_BASE_URL}/postgresql/categories`,
  BRANDS: `${API_BASE_URL}/postgresql/brands`,
  WAREHOUSES: `${API_BASE_URL}/postgresql/warehouses`,
  REVIEWS: `${API_BASE_URL}/postgresql/reviews`,
  ORDERS: `${API_BASE_URL}/postgresql/orders`,
  STRIPE: `${API_BASE_URL}/postgresql/stripe`,
  COUPONS: `${API_BASE_URL}/postgresql/coupons`,
};

// API Client Functions
export const apiClient = {
  async get(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      method: "GET",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
    });

    if (!response.ok) {
      const error = await response
        .json()
        .catch(() => ({ message: "Request failed" }));
      throw new Error(error.message || "Request failed");
    }

    return response.json();
  },

  async post(url, data, options = {}) {
    const response = await fetch(url, {
      ...options,
      method: "POST",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      let errorMessage = "Request failed";
      try {
        const error = await response.json();
        // Backend returns { error: "message" } format
        errorMessage = error.message || error || "Request failed";
      } catch (e) {
        // If JSON parsing fails, try to get text
        try {
          const text = await response.text();
          errorMessage = text || "Request failed";
        } catch (textError) {
          errorMessage = `Request failed with status ${response.status}`;
        }
      }
      throw new Error(errorMessage);
    }

    return response.json();
  },

  async delete(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      method: "DELETE",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
    });

    if (!response.ok) {
      const error = await response
        .json()
        .catch(() => ({ message: "Request failed" }));
      throw new Error(error.message || "Request failed");
    }

    return response.json();
  },
};

// Auth API
export const authAPI = {
  getCurrentUser: () => apiClient.get(`${API_ENDPOINTS.AUTH}/current-user`),
  login: (email, password) =>
    apiClient.post(`${API_ENDPOINTS.AUTH}/login`, { email, password }),
  register: userData =>
    apiClient.post(`${API_ENDPOINTS.AUTH}/register`, userData),
  logout: () => apiClient.delete(`${API_ENDPOINTS.AUTH}/logout`),
};

// Products API
export const productsAPI = {
  getAll: () => apiClient.get(API_ENDPOINTS.PRODUCTS),
  getById: id => apiClient.get(`${API_ENDPOINTS.PRODUCTS}/${id}`),
  getBestSelling: () => apiClient.get(`${API_ENDPOINTS.PRODUCTS}/best-selling`),
  getBestReviewed: () =>
    apiClient.get(`${API_ENDPOINTS.PRODUCTS}/best-reviewed`),

  /**
   * Get paginated products with filtering and search
   * @param {Object} params - Query parameters
   * @param {number} params.page - Page number (0-based)
   * @param {number} params.size - Items per page
   * @param {string} params.search - Search term
   * @param {string} params.categoryId - Category UUID filter
   * @param {string} params.brandId - Brand UUID filter
   * @param {string} params.sortBy - Sort field (name, price, sku)
   * @param {string} params.sortDirection - Sort direction (asc, desc)
   * @returns {Promise<Object>} Paginated response with content, totalPages, etc.
   */
  getPaginated: (params = {}) => {
    const queryParams = new URLSearchParams();

    if (params.page !== undefined) queryParams.set("page", params.page);
    if (params.size !== undefined) queryParams.set("size", params.size);
    if (params.search) queryParams.set("search", params.search);
    if (params.categoryId) queryParams.set("categoryId", params.categoryId);
    if (params.brandId) queryParams.set("brandId", params.brandId);
    if (params.sortBy) queryParams.set("sortBy", params.sortBy);
    if (params.sortDirection)
      queryParams.set("sortDirection", params.sortDirection);

    const queryString = queryParams.toString();
    const url = `${API_ENDPOINTS.PRODUCTS}/paginated${
      queryString ? `?${queryString}` : ""
    }`;
    return apiClient.get(url);
  },
};

// Categories API
export const categoriesAPI = {
  getAll: () => apiClient.get(API_ENDPOINTS.CATEGORIES),
  getById: id => apiClient.get(`${API_ENDPOINTS.CATEGORIES}/${id}`),
};

// Brands API
export const brandsAPI = {
  getAll: () => apiClient.get(API_ENDPOINTS.BRANDS),
  getById: id => apiClient.get(`${API_ENDPOINTS.BRANDS}/${id}`),
};

// Reviews API
export const reviewsAPI = {
  getByProductId: productId =>
    apiClient.get(`${API_ENDPOINTS.REVIEWS}/product/${productId}`),
};

// Orders API
export const ordersAPI = {
  getByUser: userId =>
    apiClient.get(`${API_ENDPOINTS.ORDERS}/by-user?userId=${userId}`),
};

// Coupons API
export const couponsAPI = {
  validate: (couponCode, orderSubtotal) => {
    return apiClient.post(`${API_ENDPOINTS.COUPONS}/validate`, {
      couponCode,
      orderSubtotal,
    });
  },
};

// Stripe API
export const stripeAPI = {
  createCheckout: (cart, address, successUrl, cancelUrl, couponCode) => {
    const body = { cart, successUrl, cancelUrl };
    if (address) body.address = address;
    if (couponCode) body.couponCode = couponCode;
    return apiClient.post(`${API_ENDPOINTS.STRIPE}/checkout`, body);
  },
};
