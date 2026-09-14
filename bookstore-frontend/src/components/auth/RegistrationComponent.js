import React, { useState } from 'react';
import { Form, Button, Alert } from 'react-bootstrap';
import { registrationInvocation } from '../../client/WebClient';

export function RegistrationComponent({ onSuccess, onSwitchToLogin }) {
  const [formData, setFormData] = useState({
    email: '',
    firstName: '',
    lastName: '',
    password: ''
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [registrationSuccess, setRegistrationSuccess] = useState(false);

  const validateEmail = (email) => {
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailPattern.test(email);
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!validateEmail(formData.email)) {
      newErrors.email = 'Invalid email format';
    }

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    }

    if (!formData.password.trim()) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
    setServerError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    setRegistrationSuccess(false);

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await registrationInvocation(formData);

      if (response.ok) {
        setRegistrationSuccess(true);
        setFormData({
          email: '',
          firstName: '',
          lastName: '',
          password: ''
        });

        // Automatically switch to login tab after 2 seconds
        setTimeout(() => {
          if (onSwitchToLogin) {
            onSwitchToLogin();
          }
        }, 2000);
      } else {
        const errorData = await response.json();
        if (response.status === 409) {
          setServerError('Email already registered. Please use a different email or login.');
        } else {
          setServerError(errorData.message || 'Registration failed');
        }
      }
    } catch (error) {
      setServerError('Network error. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Form onSubmit={handleSubmit}>
      {serverError && (
        <Alert variant="danger" className="mb-3">
          {serverError}
        </Alert>
      )}

      {registrationSuccess && (
        <Alert variant="success" className="mb-3">
          Registration successful! Redirecting to login...
        </Alert>
      )}

      <Form.Group className="mb-3">
        <Form.Label>Email <span className="text-danger">*</span></Form.Label>
        <Form.Control
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          isInvalid={!!errors.email}
          placeholder="Enter your email"
        />
        <Form.Control.Feedback type="invalid">
          {errors.email}
        </Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>First Name <span className="text-danger">*</span></Form.Label>
        <Form.Control
          type="text"
          name="firstName"
          value={formData.firstName}
          onChange={handleChange}
          isInvalid={!!errors.firstName}
          placeholder="Enter your first name"
        />
        <Form.Control.Feedback type="invalid">
          {errors.firstName}
        </Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>Last Name</Form.Label>
        <Form.Control
          type="text"
          name="lastName"
          value={formData.lastName}
          onChange={handleChange}
          placeholder="Enter your last name"
        />
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>Password <span className="text-danger">*</span></Form.Label>
        <Form.Control
          type="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          isInvalid={!!errors.password}
          placeholder="Create a password"
        />
        <Form.Control.Feedback type="invalid">
          {errors.password}
        </Form.Control.Feedback>
      </Form.Group>

      <Button
        variant="primary"
        type="submit"
        className="w-100"
        disabled={isSubmitting || registrationSuccess}
      >
        {isSubmitting ? 'Registering...' : 'Register'}
      </Button>
    </Form>
  );
}
