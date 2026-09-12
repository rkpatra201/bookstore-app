import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

function TopNavbar({ cartCount = 0 }) {
  return (
    <>
      {/* 1. The Sticky Top Navbar Structure */}
      <Navbar 
        bg="dark" 
        variant="dark" 
        expand="md" 
        fixed="top" 
        className="shadow-sm px-3 border-bottom border-secondary"
      >
        <Container>
          {/* Brand/Logo redirecting back to home page */}
          <LinkContainer to="/">
            <Navbar.Brand className="fw-bold fs-4 cursor-pointer">
              📚 Bookstore
            </Navbar.Brand>
          </LinkContainer>

          <Navbar.Toggle aria-controls="top-navbar-nav" />
          
          <Navbar.Collapse id="top-navbar-nav" className="justify-content-end">
            <Nav className="align-items-center gap-2 mt-2 mt-md-0">
              
              {/* Button A: Orders Navigation Link */}
              <LinkContainer to="/orders">
                <Button variant="outline-light" className="fw-medium px-3 btn-sm border-0">
                  📦 Orders
                </Button>
              </LinkContainer>

              {/* Button B: Cart Navigation Link with Badge */}
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

              {/* Button C: "Me" / User Profile Navigation Link */}
              <LinkContainer to="/profile">
                <Button variant="primary" className="fw-semibold px-3 btn-sm rounded-pill shadow-sm">
                  👤 Me
                </Button>
              </LinkContainer>

            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      {/* 2. CRITICAL FIX: Spacer element to prevent layout content from hiding beneath the fixed navbar */}
      <div style={{ paddingTop: '70px' }} />
    </>
  );
}

export default TopNavbar;
