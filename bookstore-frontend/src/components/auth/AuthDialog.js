import React, { useState, useEffect } from 'react';
import { Modal, Tab, Tabs, Alert } from 'react-bootstrap';
import { LoginComponent } from './LoginComponent';
import { RegistrationComponent } from './RegistrationComponent';
import { useNavigate } from 'react-router-dom';

export function AuthDialog({ show, onHide, unauthorizedMessage }) {
  const [activeTab, setActiveTab] = useState('login');
  const navigate = useNavigate();

  // Reset to login tab when dialog opens with unauthorized message
  useEffect(() => {
    if (show && unauthorizedMessage) {
      setActiveTab('login');
    }
  }, [show, unauthorizedMessage]);

  const handleLoginSuccess = () => {
    onHide();
    navigate('/');
  };

  const handleRegistrationSuccess = () => {
    setActiveTab('login');
  };

  const handleSwitchToLogin = () => {
    setActiveTab('login');
  };

  return (
    <Modal show={show} onHide={onHide} centered>
      <Modal.Header closeButton>
        <Modal.Title>Authentication</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {unauthorizedMessage && (
          <Alert variant="warning" className="mb-3">
            {unauthorizedMessage}
          </Alert>
        )}
        <Tabs
          activeKey={activeTab}
          onSelect={(k) => setActiveTab(k)}
          className="mb-3"
          justify
        >
          <Tab eventKey="login" title="Login">
            <LoginComponent
              onSuccess={handleLoginSuccess}
              onClose={onHide}
            />
          </Tab>
          <Tab eventKey="registration" title="Registration">
            <RegistrationComponent
              onSuccess={handleRegistrationSuccess}
              onSwitchToLogin={handleSwitchToLogin}
            />
          </Tab>
        </Tabs>
      </Modal.Body>
    </Modal>
  );
}
