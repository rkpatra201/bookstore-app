import React, { useState, useEffect } from 'react';
import { Table, Button, Container, Row, Col, Card, Spinner, Alert } from 'react-bootstrap';
import { CART_URL } from '../../constants/AppConstants';
import { useCart } from '../../providers/CartProvider';
import { addToCartInvocation, deleteLineItemInvocation, orderInvocation, reduceFromCartInvocation } from '../../client/WebClient';
import { AddressCardComponent, AddressComponent, AddressListComponent } from '../me/AddressComponent';
import { useNavigate } from 'react-router-dom';

export function Cart() {
    // 1. Data management states
    const [cartData, setCartData] = useState(null);
    const [address, setAddress] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [serverMessage, setServerMessage] = useState('');
    const [checkoutSuccess, setCheckoutSuccess] = useState(false);

    // Hook provider to sync context navbar values later if required
    const { cartCount, incrementCart, decrementCart, setCart } = useCart();

    const navigate = useNavigate();


    // 2. Fetch authoritative server-side cart state on mount (runs exactly once due to [])
    useEffect(() => {
        fetch(CART_URL) // Adjust mapping port configuration if needed
            .then((res) => {
                if (!res.ok) {
                    throw new Error('Failed to retrieve cart items from server');
                }
                return res.json();
            })
            .then((payload) => {
                // Unwraps the standard uniform DataResponse shell container mapping parameter (.data)
                if (payload.success && payload.data) {
                    setCartData(payload.data);
                } else {
                    setCartData(payload);
                }
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, [cartCount]);


    // Placeholders for update execution actions (as requested, to be wired next)
    const handleIncrement = (item) => {
        console.log("Trigger increment for itemId:", item);
        addToCartInvocation(item)
            .then(res => res.json())
            .then(json => incrementCart(item))
    };

    const handleDecrement = (item) => {
        console.log("Trigger decrement for itemId:", item);
        reduceFromCartInvocation(item)
            .then(res => res.json())
            .then(json => decrementCart(item))
    };

    const handleDelete = (item) => {
        console.log("Trigger removal delete for itemId:", item);
        deleteLineItemInvocation(item.itemId)
            .then(res => res.json())
            .then(data => setCart(cartCount - item.quantity))
    };

    const addressSelectionHandler = (addr) => {
        setAddress(addr);
    }

    const handleCheckout = async (e) => {
        console.log('Form Submit : Save Checkout')
        e.preventDefault(); // Stop default browser full-page reload triggers
        setServerMessage(null);

        // Validate parameters locally before dispatching HTTP network requests
        if (!address.id) {
            alert('Please select valid address for shipping');
            return
        };

        let isCheckoutSuccess = false;
        try {
            const response = await orderInvocation().checkout(address.id);
            const payload = await response.json();
            if (response.ok && payload.success) {
                setServerMessage({ type: 'success', text: 'Order placed successfully!' });
            } else {
                throw new Error(payload.message || 'Server rejected order validation mapping parameters.');
            }
           isCheckoutSuccess = true;
        } catch (error) {
            setServerMessage({ type: 'danger', text: error.message });
        } finally {
            setTimeout(() => {
                setServerMessage(null);
                if(isCheckoutSuccess) setCart(0)
                setCheckoutSuccess(isCheckoutSuccess);
            }, 5000);
        }
    };

    // 3. Conditional boundary state display screens
    if (loading) {
        return (
            <Container className="text-center my-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2 text-muted">Loading your active cart items...</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className="my-4">
                <Alert variant="danger">Cart Error: {error}</Alert>
            </Container>
        );
    }

    const lineItems = cartData?.lineItems || [];

    return (
        <Container className="my-5">
            <h2 className="fw-bold mb-4 text-start">🛒 Shopping Cart</h2>

            {lineItems.length === 0 ? (
                <Alert variant="info" className="text-center py-5 shadow-sm">
                    <h4>Your cart is empty</h4>
                    <p className="text-muted mb-0">Head back to the catalog to choose your favorite titles!</p>
                </Alert>
            ) : <>
                <Row>
                    {/* Main Items Listing Grid Column */}
                    <Col lg={8} className="mb-4">
                        <div className="shadow-sm rounded border overflow-hidden bg-white">
                            <Table hover responsive className="align-middle mb-0 text-nowrap">
                                <thead className="table-dark">
                                    <tr>
                                        <th scope="col">Book Title</th>
                                        <th scope="col" className="text-end" style={{ width: '100px' }}>Price</th>
                                        <th scope="col" className="text-center" style={{ width: '140px' }}>Quantity</th>
                                        <th scope="col" className="text-end" style={{ width: '120px' }}>Subtotal</th>
                                        <th scope="col" className="text-center" style={{ width: '80px' }}>Remove</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {lineItems.map((item) => (
                                        <tr key={item.id}>
                                            {/* Title Segment */}
                                            <td className="fw-semibold text-wrap" style={{ maxWidth: '250px' }}>
                                                {item.title}
                                            </td>

                                            {/* Unit Financial Parameters */}
                                            <td className="text-end">${Number(item.unitPrice).toFixed(2)}</td>

                                            {/* Interactive Increment / Decrement Count Section */}
                                            <td className="text-center">
                                                <div className="d-flex align-items-center justify-content-center gap-2">
                                                    <Button
                                                        variant="outline-secondary"
                                                        size="sm"
                                                        className="px-2 py-0 fw-bold"
                                                        onClick={() => handleDecrement(item)}
                                                    >
                                                        -
                                                    </Button>
                                                    <span className="fw-semibold px-2" style={{ minWidth: '24px' }}>
                                                        {item.quantity}
                                                    </span>
                                                    <Button
                                                        variant="outline-secondary"
                                                        size="sm"
                                                        className="px-2 py-0 fw-bold"
                                                        onClick={() => handleIncrement(item)}
                                                    >
                                                        +
                                                    </Button>
                                                </div>
                                            </td>

                                            {/* Line Subtotal */}
                                            <td className="text-end fw-bold text-dark">
                                                ${Number(item.subTotal).toFixed(2)}
                                            </td>

                                            {/* Delete Operation Button Component */}
                                            <td className="text-center">
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    className="border-0"
                                                    onClick={() => handleDelete(item)}
                                                >
                                                    🗑️
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    </Col>

                    {/* Pricing Summary Sidepanel Column Block */}
                    <Col lg={4}>
                        {serverMessage && (
                            <Alert variant={serverMessage.type} className="mb-4 shadow-sm">
                                {serverMessage.text}
                            </Alert>
                        )}
                        <Card className="shadow-sm border border-light p-3 position-sticky" style={{ top: '90px' }}>
                            <Card.Body>
                                <h4 className="fw-bold border-bottom pb-2 mb-3">Order Summary</h4>
                                <div className="d-flex justify-content-between mb-3 text-muted">
                                    <span>Total Items</span>
                                    <span className="fw-medium">
                                        {lineItems.reduce((acc, curr) => acc + curr.quantity, 0)}
                                    </span>
                                </div>
                                <div className="d-flex justify-content-between border-top pt-3 mb-4 fs-5 fw-bold text-success">
                                    <span>Grand Total</span>
                                    <span>${Number(cartData?.totalCartPrice).toFixed(2)}</span>
                                </div>
                                <Button variant="success" onClick={handleCheckout} size="lg" className="w-100 fw-bold py-2 shadow-sm">
                                    Proceed to Checkout
                                </Button>
                                {
                                    checkoutSuccess &&
                                    navigate('/')

                                }
                            </Card.Body>
                        </Card>
                    </Col>

                    <Col lg={8}>
                        <AddressListComponent refreshTrigger={true} enableDelete={false} addressSelectionHandler={addressSelectionHandler} />
                    </Col>

                    {address.id && <Col lg={4}>
                        <AddressCardComponent enableDelete={false} addr={address} />
                    </Col>
                    }







                </Row>



            </>
            }
        </Container>
    );
}

export default Cart;
