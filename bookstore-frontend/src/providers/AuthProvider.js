import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [authToken, setAuthToken] = useState(null);
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Load token from localStorage on mount
  useEffect(() => {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('userData');
    if (token) {
      setAuthToken(token);
      setIsAuthenticated(true);
      if (userData) {
        setUser(JSON.parse(userData));
      }
    }
  }, []);

  const login = (token, userData) => {
    localStorage.setItem('authToken', token);
    if (userData) {
      localStorage.setItem('userData', JSON.stringify(userData));
      setUser(userData);
    }
    setAuthToken(token);
    setIsAuthenticated(true);
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    setAuthToken(null);
    setUser(null);
    setIsAuthenticated(false);
  };

  const getAuthToken = () => {
    return authToken || localStorage.getItem('authToken');
  };

  return (
    <AuthContext.Provider value={{
      authToken,
      user,
      isAuthenticated,
      login,
      logout,
      getAuthToken
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
