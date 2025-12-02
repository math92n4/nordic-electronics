# Nordic Electronics Frontend

This is a React application using TanStack Query for data fetching and state management.

## Project Structure

The frontend source code is located in the `frontend/` directory at the project root. When built, the output goes to `src/main/resources/static/` where Spring Boot serves it.

```
nordic-electronics/
├── frontend/              # React source code (source)
│   ├── src/
│   │   ├── components/   # React components
│   │   ├── hooks/        # Custom React hooks
│   │   ├── utils/        # Utility functions
│   │   ├── api.js        # API client
│   │   ├── App.jsx       # Main app component
│   │   └── main.jsx      # Entry point
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── src/main/resources/static/  # Build output (generated)
    ├── css/              # CSS files (kept as-is)
    ├── index.html        # Built HTML
    └── js/               # Built JavaScript bundles
```

## Setup

1. Install dependencies:
```bash
cd frontend
npm install
```

2. For development (with hot reload):
```bash
npm run dev
```

This starts Vite dev server on port 3000 with hot module replacement. The proxy configuration forwards `/api` requests to your Spring Boot backend running on `http://localhost:8080`.

3. For production build:
```bash
npm run build
```

The build output will be written to `src/main/resources/static/`, which is where Spring Boot serves static files from.

## Development Workflow

1. **Development**: Run `npm run dev` in the `frontend/` directory. This gives you hot reload and a separate dev server.

2. **Production**: Run `npm run build` to build the app, then start your Spring Boot application. The built files will be served from `/static`.

## Architecture

- **React**: UI framework
- **TanStack Query**: Data fetching, caching, and synchronization
- **Vite**: Build tool and dev server
- **Custom Hooks**: 
  - `useAuth`: Authentication state and operations
  - `useCart`: Shopping cart state management with localStorage persistence

## Components

- `Navbar`: Navigation bar with cart icon and auth link
- `AuthModal`: Login and registration modal
- `CartModal`: Shopping cart modal with checkout
- `ProductsSection`: Product listing with filters
- `CategoriesSection`: Category display
- `BrandsSection`: Brand display
- `OrdersSection`: User orders display

## API Integration

All API calls are centralized in `src/api.js` and use TanStack Query for:
- Automatic caching
- Background refetching
- Loading and error states
- Optimistic updates
