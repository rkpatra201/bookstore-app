import { ADDRESS_URL, CART_URL, ORDER_URL } from "../constants/AppConstants";

export function addressInvocation() {
    return {
        list: loadAddressInvocation,
        deletAddress: deleteAddressInvocation,
        saveAddress: saveAddressInvocation
    }
}

export function orderInvocation(){
 return {
    checkout: processCheckout
 }
}

function processCheckout(addressId, paymentMethod){
  const payload = {
        addressId: addressId,
        paymentMethod: paymentMethod
    };

    return fetch(ORDER_URL+'/checkout', {
        method: 'POST', // Specify the HTTP method
        headers: {
            'Content-Type': 'application/json' // Tell the server you're sending JSON
        },
        body: JSON.stringify(payload) // Convert the JS object into a JSON string
    });
}

export function loadCartDataInvocation() {
    return fetch(CART_URL);
}
export function deleteLineItemInvocation(id) {
    return fetch(CART_URL + '/' + id, {
        method: 'DELETE'
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
    return fetch(CART_URL, {
        method: 'POST', // Specify the HTTP method
        headers: {
            'Content-Type': 'application/json' // Tell the server you're sending JSON
        },
        body: JSON.stringify(payload) // Convert the JS object into a JSON string
    });
}

export function reduceFromCartInvocation(item) {
    const payload = {
        itemId: item.itemId,
        quantity: -1
    };
    return fetch(CART_URL + '/' + payload.itemId, {
        method: 'PATCH', // Specify the HTTP method
        headers: {
            'Content-Type': 'application/json' // Tell the server you're sending JSON
        },
        body: JSON.stringify(payload) // Convert the JS object into a JSON string
    });
}


function deleteAddressInvocation(id) {
    return fetch(ADDRESS_URL + '/' + id, {
        method: 'DELETE'
    })
}

function loadAddressInvocation(id) {
    return fetch(ADDRESS_URL, {
        method: 'GET'
    })
}

function saveAddressInvocation(address) {
    return fetch(ADDRESS_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(address)
    });
}