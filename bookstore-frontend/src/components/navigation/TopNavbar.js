import React, { useState, useEffect } from 'react';
import { Navbar, Nav, Container, Button, Toast } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { useAuth } from '../../providers/AuthProvider';
import { AuthDialog } from '../auth/AuthDialog';
import { useNavigate } from 'react-router-dom';
import { CartNavButton } from './CartNavButton';
import { OrdersNavButton } from './OrdersNavButton';
import { MeNavButton } from './MeNavButton';

function TopNavbar() {
  const { isAuthenticated, logout } = useAuth();
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [unauthorizedMessage, setUnauthorizedMessage] = useState('');
  const navigate = useNavigate();

  // Listen for 401 unauthorized events from WebClient
  useEffect(() => {
    const handleUnauthorized = (event) => {
      console.log('401 Unauthorized detected - showing login dialog');
      logout(); // Clear auth state in provider
      setUnauthorizedMessage(event.detail?.message || 'Session expired');
      setShowAuthDialog(true);
      navigate('/');
    };

    window.addEventListener('unauthorized', handleUnauthorized);

    return () => {
      window.removeEventListener('unauthorized', handleUnauthorized);
    };
  }, [logout, navigate]);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleAuthDialogClose = () => {
    setShowAuthDialog(false);
    setUnauthorizedMessage('');
  };
  return (
    <>
      <Navbar bg="dark" variant="dark" expand="md" fixed="top" className="shadow-sm px-3 border-bottom border-secondary">
        <Container>
          <LinkContainer to="/">
            <Navbar.Brand className="fw-bold fs-4 cursor-pointer">📚 Bookstore</Navbar.Brand>
          </LinkContainer>
          <Navbar.Toggle aria-controls="top-navbar-nav" />
          <Navbar.Collapse id="top-navbar-nav" className="justify-content-end">
            <Nav className="align-items-center gap-2 mt-2 mt-md-0">
              <OrdersNavButton />
              <CartNavButton />

              {!isAuthenticated ? (
                <Button
                  variant="success"
                  className="fw-semibold px-3 btn-sm rounded-pill shadow-sm"
                  onClick={() => setShowAuthDialog(true)}
                >
                  🔐 Registration / Login
                </Button>
              ) : (
                <Button
                  variant="danger"
                  className="fw-semibold px-3 btn-sm rounded-pill shadow-sm"
                  onClick={handleLogout}
                >
                  🚪 Logout
                </Button>
              )}

              <MeNavButton />
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
      <div style={{ paddingTop: '70px' }} />

      <AuthDialog
        show={showAuthDialog}
        onHide={handleAuthDialogClose}
        unauthorizedMessage={unauthorizedMessage}
      />
    </>
  );
}

export default TopNavbar;
