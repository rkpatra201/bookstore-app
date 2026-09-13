import React, { useState } from 'react';
import { Container, Row, Col, Card } from 'react-bootstrap';
import { ProfileComponent } from './ProfileComponent';
import { AddressComponent } from './AddressComponent';

export function MeComponent() {
  // Track which view is currently selected ('profile', 'addresses', or null)
  const [activeView, setActiveView] = useState('profile');

  return (
    <Container className="my-5 text-start">
      {/* 1. Header Section */}
      <h2 className="fw-bold text-dark mb-4">👤 My Dashboard</h2>

      {/* 2. Top Button Boxes Grid Layout */}
      <Row className="mb-5">
        
        {/* Box Button A: View Profile */}
        <Col xs={12} sm={6} className="mb-3">
          <Card 
            className={`shadow-sm border-2 cursor-pointer h-100 transition-all ${
              activeView === 'profile' ? 'border-primary bg-primary-subtle' : 'border-light bg-white'
            }`}
            onClick={() => setActiveView('profile')}
            style={{ cursor: 'pointer', transition: 'all 0.2s ease-in-out' }}
          >
            <Card.Body className="p-4 text-center">
              <div className="fs-1 mb-2">👤</div>
              <h4 className="fw-bold mb-1 text-dark">My Profile</h4>
              <p className="text-muted small mb-0">Update account personal details and preferences</p>
            </Card.Body>
          </Card>
        </Col>

        {/* Box Button B: View Addresses */}
        <Col xs={12} sm={6} className="mb-3">
          <Card 
            className={`shadow-sm border-2 cursor-pointer h-100 transition-all ${
              activeView === 'addresses' ? 'border-primary bg-primary-subtle' : 'border-light bg-white'
            }`}
            onClick={() => setActiveView('addresses')}
            style={{ cursor: 'pointer', transition: 'all 0.2s ease-in-out' }}
          >
            <Card.Body className="p-4 text-center">
              <div className="fs-1 mb-2">🏡</div>
              <h4 className="fw-bold mb-1 text-dark">My Addresses</h4>
              <p className="text-muted small mb-0">Manage shipping addresses for fast checkouts</p>
            </Card.Body>
          </Card>
        </Col>

      </Row>

      {/* 3. Bottom Dynamic Content Container Panel */}
      <Row>
        <Col xs={12}>
          {/* <div className="p-4 bg-white rounded border shadow-sm" style={{ minHeight: '300px' }}> */}
            {activeView === 'profile' && <ProfileComponent />}
            {activeView === 'addresses' && <AddressComponent />}
          {/* </div> */}
        </Col>
      </Row>
    </Container>
  );
}

export default MeComponent;
