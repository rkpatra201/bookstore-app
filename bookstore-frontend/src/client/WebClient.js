import { ADDRESS_URL, CART_URL, CATALOG_URL, ORDER_URL, USER_ACCOUNT_URL } from "../constants/AppConstants";

// Global 401 handler - dispatch custom event when unauthorized
function handle401Response(response) {
    if (response.status === 401) {
        // Clear authentication data
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');

        // Dispatch custom event to trigger login dialog
        window.dispatchEvent(new CustomEvent('unauthorized', {
            detail: { message: 'Session expired. Please login again.' }
        }));
    }
    return response;
}

// Wrapper for fetch that includes 401 handling
function fetchWithAuth(url, options = {}) {
    return fetch(url, options)
        .then(response => {
            handle401Response(response);
            return response;
        })
        .catch(error => {
            // Handle network errors (CORS, server down, etc.)
            console.error('Network error:', error);
            // Re-throw the error so calling code can handle it
            throw error;
        });
}

export function addressInvocation() {
    return {
        list: loadAddressInvocation,
        deletAddress: deleteAddressInvocation,
        saveAddress: saveAddressInvocation
    }
}

export function orderInvocation(){
 return {
    checkout: processCheckout,
    list: loadOrdersInvocation,
    getById: getOrderByIdInvocation
 }
}

export function catalogInvocation() {
    return {
        list: loadCatalogInvocation
    }
}

function processCheckout(addressId, paymentMethod){
  const payload = {
        addressId: addressId,
        paymentMethod: paymentMethod
    };

    return fetchWithAuth(ORDER_URL+'/checkout', {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload)
    });
}

export function loadCartDataInvocation() {
    return fetchWithAuth(CART_URL, {
        headers: getAuthHeaders()
    });
}
export function deleteLineItemInvocation(id) {
    return fetchWithAuth(CART_URL + '/' + id, {
        method: 'DELETE',
        headers: getAuthHeaders()
    })
}

export function addToCartInvocation(item) {
    const payload = {
        itemId: item.itemId,
        quantity: 1
    };
    if (payload.itemId === undefined) {
        payload.itemId = item.id
    }
    return fetchWithAuth(CART_URL, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload)
    });
}

export function reduceFromCartInvocation(item) {
    const payload = {
        itemId: item.itemId,
        quantity: -1
    };
    return fetchWithAuth(CART_URL + '/' + payload.itemId, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload)
    });
}


function deleteAddressInvocation(id) {
    return fetchWithAuth(ADDRESS_URL + '/' + id, {
        method: 'DELETE',
        headers: getAuthHeaders()
    })
}

function loadAddressInvocation(id) {
    return fetchWithAuth(ADDRESS_URL, {
        method: 'GET',
        headers: getAuthHeaders()
    })
}

function saveAddressInvocation(address) {
    return fetchWithAuth(ADDRESS_URL, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(address)
    });
}

// Authentication API methods
export function registrationInvocation(userData) {
    return fetchWithAuth(USER_ACCOUNT_URL + '/registration', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData)
    });
}

export function loginInvocation(credentials) {
    return fetchWithAuth(USER_ACCOUNT_URL + '/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials)
    });
}

// Helper function to get auth headers
export function getAuthHeaders() {
    const token = localStorage.getItem('authToken');
    const headers = {
        'Content-Type': 'application/json'
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}

// Order API methods
function loadOrdersInvocation() {
    return fetchWithAuth(ORDER_URL, {
        method: 'GET',
        headers: getAuthHeaders()
    });
}

function getOrderByIdInvocation(orderId) {
    return fetchWithAuth(`${ORDER_URL}/${orderId}`, {
        method: 'GET',
        headers: getAuthHeaders()
    });
}

// Catalog API methods
function loadCatalogInvocation() {
    return fetchWithAuth(CATALOG_URL, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    });
}

// Payment API methods (placeholder for future implementation)
export function processPaymentInvocation(orderId) {
    return fetchWithAuth(`${ORDER_URL}/${orderId}/payment`, {
        method: 'POST',
        headers: getAuthHeaders()
    });
}