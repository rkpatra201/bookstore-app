import React from 'react';
import { Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { useAuth } from '../../providers/AuthProvider';

export function MeNavButton() {
  const { isAuthenticated } = useAuth();

  // Only show Me button if authenticated
  if (!isAuthenticated) {
    return null;
  }

  return (
    <LinkContainer to="/me">
      <Button variant="primary" className="fw-semibold px-3 btn-sm rounded-pill shadow-sm">
        👤 Me
      </Button>
    </LinkContainer>
  );
}
