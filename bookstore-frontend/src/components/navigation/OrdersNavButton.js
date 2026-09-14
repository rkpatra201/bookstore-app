import React from 'react';
import { Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { useAuth } from '../../providers/AuthProvider';

export function OrdersNavButton() {
  const { isAuthenticated } = useAuth();

  // Only show Orders button if authenticated
  if (!isAuthenticated) {
    return null;
  }

  return (
    <LinkContainer to="/orders">
      <Button variant="outline-light" className="fw-medium px-3 btn-sm border-0">
        📦 Orders
      </Button>
    </LinkContainer>
  );
}
