import React, { createContext, useContext, useState } from 'react';

// Create the Cart Context
const CartContext = createContext();

// Cart Provider component - manages cart count state
export function CartProvider({ children }) {
    const [cartCount, setCartCount] = useState(0);

    // Direct setter for cart count (used by components that receive full cart data from API)
    const setCart = (count) => {
        setCartCount(count);
    };

    // Reset cart count to 0 (used after checkout)
    const clearCart = () => {
        setCartCount(0);
    };

    return (
        <CartContext.Provider value={{ cartCount, setCart, clearCart }}>
            {children}
        </CartContext.Provider>
    );
}

// 3. Custom utility hook for clean consumer imports
export function useCart() {
    return useContext(CartContext);
}
