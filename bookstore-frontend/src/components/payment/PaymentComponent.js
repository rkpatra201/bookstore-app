import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Card, Row, Col, Button, Spinner, Alert, Table } from 'react-bootstrap';
import { ORDER_URL } from '../../constants/AppConstants';

export function Payment() {
    const { orderId } = useParams();
    const navigate = useNavigate();

    const [orderSummary, setOrderSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [processingPayment, setProcessingPayment] = useState(false);
    const [paymentMessage, setPaymentMessage] = useState(null);

    // Fetch order summary on component mount
    useEffect(() => {
        if (!orderId) {
            setError('No order ID provided');
            setLoading(false);
            return;
        }

        fetch(`${ORDER_URL}/${orderId}`)
            .then((res) => {
                if (!res.ok) {
                    throw new Error('Failed to retrieve order summary');
                }
                return res.json();
            })
            .then((payload) => {
                if (payload.success && payload.data) {
                    setOrderSummary(payload.data);
                } else {
                    setOrderSummary(payload);
                }
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, [orderId]);

    const handleCompletePayment = async () => {
        setProcessingPayment(true);
        setPaymentMessage(null);

        try {
            // TODO: Replace with actual payment processing endpoint when available
            // For now, simulate payment completion
            setPaymentMessage({ type: 'success', text: 'Payment completed successfully! Redirecting...' });
            setTimeout(() => {
                navigate('/orders');
            }, 2000);

            /* Uncomment when payment API is ready:
            const response = await fetch(`${ORDER_URL}/${orderId}/payment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const payload = await response.json();

            if (response.ok && payload.success) {
                setPaymentMessage({ type: 'success', text: 'Payment completed successfully! Redirecting...' });
                setTimeout(() => {
                    navigate('/orders');
                }, 2000);
            } else {
                throw new Error(payload.message || 'Payment processing failed');
            }
            */
        } catch (error) {
            setPaymentMessage({ type: 'danger', text: error.message });
            setProcessingPayment(false);
        }
    };

    if (loading) {
        return (
            <Container className="text-center my-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2 text-muted">Loading payment details...</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className="my-4">
                <Alert variant="danger">Error: {error}</Alert>
                <Button variant="primary" onClick={() => navigate('/')}>
                    Return to Home
                </Button>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <h2 className="fw-bold mb-4 text-start">Payment</h2>

            {paymentMessage && (
                <Alert variant={paymentMessage.type} className="mb-4">
                    {paymentMessage.text}
                </Alert>
            )}

            <Row>
                <Col lg={8}>
                    <Card className="shadow-sm mb-4">
                        <Card.Header className="bg-primary text-white">
                            <h5 className="mb-0">Order Summary</h5>
                        </Card.Header>
                        <Card.Body>
                            <div className="mb-3">
                                <strong>Order ID:</strong> {orderSummary?.orderId || orderId}
                            </div>
                            <div className="mb-3">
                                <strong>Status:</strong>{' '}
                                <span className="badge bg-info">
                                    {orderSummary?.status || orderSummary?.orderStatus || 'PENDING'}
                                </span>
                            </div>

                            {orderSummary?.lineItems && orderSummary.lineItems.length > 0 && (
                                <div className="mt-4">
                                    <h6 className="fw-bold mb-3">Items</h6>
                                    <Table striped bordered hover responsive>
                                        <thead>
                                            <tr>
                                                <th>Book Title</th>
                                                <th className="text-end">Price</th>
                                                <th className="text-center">Quantity</th>
                                                <th className="text-end">Subtotal</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {orderSummary.lineItems.map((item, index) => (
                                                <tr key={index}>
                                                    <td>{item.title || item.bookTitle || 'N/A'}</td>
                                                    <td className="text-end">
                                                        ${Number(item.unitPrice || 0).toFixed(2)}
                                                    </td>
                                                    <td className="text-center">{item.quantity}</td>
                                                    <td className="text-end">
                                                        ${Number(item.subTotal || item.unitPrice * item.quantity).toFixed(2)}
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </Table>
                                </div>
                            )}

                            <div className="mt-4 pt-3 border-top">
                                <div className="d-flex justify-content-between fs-5 fw-bold text-success">
                                    <span>Total Amount:</span>
                                    <span>${Number(orderSummary?.totalAmount || orderSummary?.totalPrice || 0).toFixed(2)}</span>
                                </div>
                            </div>

                            {orderSummary?.shippingAddress && (
                                <div className="mt-4">
                                    <h6 className="fw-bold">Shipping Address</h6>
                                    <p className="mb-1">{orderSummary.shippingAddress.street}</p>
                                    <p className="mb-1">
                                        {orderSummary.shippingAddress.city}, {orderSummary.shippingAddress.state} {orderSummary.shippingAddress.zipCode}
                                    </p>
                                    <p className="mb-0">{orderSummary.shippingAddress.country}</p>
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                <Col lg={4}>
                    <Card className="shadow-sm border border-light p-3">
                        <Card.Body>
                            <h5 className="fw-bold mb-4">Payment Details</h5>

                            <div className="mb-4 p-3 bg-light rounded">
                                <h6 className="fw-bold mb-2">Payment Method</h6>
                                <p className="text-muted mb-0">
                                    TODO: PAYMENT_METHOD
                                </p>
                                <small className="text-muted">
                                    {orderSummary?.paymentMethod || 'Not specified'}
                                </small>
                            </div>

                            <div className="mb-3">
                                <div className="d-flex justify-content-between mb-2">
                                    <span>Subtotal:</span>
                                    <span>${Number(orderSummary?.totalAmount || 0).toFixed(2)}</span>
                                </div>
                                <div className="d-flex justify-content-between mb-2">
                                    <span>Tax:</span>
                                    <span>$0.00</span>
                                </div>
                                <div className="d-flex justify-content-between mb-2">
                                    <span>Shipping:</span>
                                    <span>Free</span>
                                </div>
                                <hr />
                                <div className="d-flex justify-content-between fs-5 fw-bold text-success">
                                    <span>Total:</span>
                                    <span>${Number(orderSummary?.totalAmount || 0).toFixed(2)}</span>
                                </div>
                            </div>

                            <Button
                                variant="primary"
                                size="lg"
                                className="w-100 fw-bold py-2 shadow-sm"
                                onClick={handleCompletePayment}
                                disabled={processingPayment}
                            >
                                {processingPayment ? (
                                    <>
                                        <Spinner
                                            as="span"
                                            animation="border"
                                            size="sm"
                                            role="status"
                                            aria-hidden="true"
                                            className="me-2"
                                        />
                                        Processing...
                                    </>
                                ) : (
                                    'SUBMIT'
                                )}
                            </Button>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
}

export default Payment;
