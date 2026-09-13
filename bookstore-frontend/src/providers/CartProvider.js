import React, { createContext, useContext, useState } from 'react';
import { CART_URL } from '../constants/AppConstants';
import { preconnect } from 'react-dom';

// 1. Create the Context radio tower
const CartContext = createContext();

// 2. Define the Provider wrapper component
export function CartProvider({ children }) {

    const [cartCount, setCartCount] = useState(0);

    const incrementCartCount = (item) => {
        setCartCount(prevCount => {
            console.log('the prevCount', prevCount)
              return prevCount + 1
        }
        );
    };

    const decrementCartCount = (item) => {
        setCartCount(prevCount => prevCount - 1);
    };

    const resetCartCount = () => {
        setCartCount(prevCount => 0);
    }

    const setCart = (count)=>{
        setCartCount(prev=> count);
    }

    return (
        <CartContext.Provider value={{ cartCount, incrementCart: incrementCartCount, decrementCart: decrementCartCount, clearCart: resetCartCount, setCart }}>
            {children}
        </CartContext.Provider>
    );
}

// 3. Custom utility hook for clean consumer imports
export function useCart() {
    return useContext(CartContext);
}
