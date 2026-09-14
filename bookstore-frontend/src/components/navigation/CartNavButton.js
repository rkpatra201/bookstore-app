import React, { useEffect } from 'react';
import { Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { useCart } from '../../providers/CartProvider';
import { useAuth } from '../../providers/AuthProvider';
import { loadCartDataInvocation } from '../../client/WebClient';

export function CartNavButton() {
  const { cartCount, setCart } = useCart();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    // Only load cart data if user is authenticated
    if (isAuthenticated) {
      loadCartDataInvocation()
        .then(res => {
          if (res.ok) {
            return res.json();
          }
          throw new Error('Failed to load cart');
        })
        .then(data => setCart(data.data.itemCount))
        .catch(err => {
          console.error('Cart load error:', err);
          setCart(0);
        });
    } else {
      // Reset cart count when not authenticated
      setCart(0);
    }
  }, [isAuthenticated, setCart]);

  return (
    <LinkContainer to="/cart">
      <Button variant="outline-light" className="fw-medium px-3 btn-sm border-0 position-relative">
        🛒 Cart
        {cartCount > 0 && (
          <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
            {cartCount}
          </span>
        )}
      </Button>
    </LinkContainer>
  );
}
