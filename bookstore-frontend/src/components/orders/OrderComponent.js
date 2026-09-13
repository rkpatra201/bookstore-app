import React, { useState, useEffect } from 'react';
import { Container, Card, Table, Button, Spinner, Alert, Badge, Row, Col, Modal } from 'react-bootstrap';
import { ORDER_URL } from '../../constants/AppConstants';

export function OrderHistory() {
    const [orders, setOrders] = useState([]);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [detailLoading, setDetailLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);

    // Fetch order list on component mount
    useEffect(() => {
        fetch(ORDER_URL)
            .then((res) => {
                if (!res.ok) {
                    throw new Error('Failed to retrieve order history');
                }
                return res.json();
            })
            .then((payload) => {
                if (payload.success && payload.data) {
                    setOrders(payload.data);
                } else {
                    setOrders([]);
                }
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    // Fetch order details when View button is clicked
    const handleViewOrder = async (orderId) => {
        setDetailLoading(true);
        setShowModal(true);
        setSelectedOrder(null);

        try {
            const response = await fetch(`${ORDER_URL}/${orderId}`);
            if (!response.ok) {
                throw new Error('Failed to retrieve order details');
            }

            const payload = await response.json();
            if (payload.success && payload.data) {
                setSelectedOrder(payload.data);
            } else {
                throw new Error('Invalid response format');
            }
        } catch (err) {
            setError(err.message);
            setShowModal(false);
        } finally {
            setDetailLoading(false);
        }
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setSelectedOrder(null);
    };

    const getStatusBadgeVariant = (status) => {
        const statusMap = {
            'RESERVED': 'secondary',
            'AWAITING_PAYMENT': 'warning',
            'PAYMENT_SUCCESS': 'success',
            'PAYMENT_ERROR': 'danger',
            'PAYMENT_TIMEOUT': 'danger',
            'CANCELLED': 'dark',
            'PROCESSING': 'info',
            'SHIPPED': 'primary',
            'DELIVERED': 'success',
            'COMPLETED': 'success',
        };
        return statusMap[status] || 'secondary';
    };

    if (loading) {
        return (
            <Container className="text-center my-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2 text-muted">Loading your order history...</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className="my-4">
                <Alert variant="danger">Error: {error}</Alert>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <h2 className="fw-bold mb-4">Order History</h2>

            {orders.length === 0 ? (
                <Alert variant="info" className="text-center py-5">
                    <h4>No orders found</h4>
                    <p className="text-muted mb-0">You haven't placed any orders yet.</p>
                </Alert>
            ) : (
                <Card className="shadow-sm">
                    <Card.Body className="p-0">
                        <Table hover responsive className="mb-0">
                            <thead className="table-dark">
                                <tr>
                                    <th>Order ID</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Payment Method</th>
                                    <th className="text-end">Total Amount</th>
                                    <th className="text-center">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {orders.map((order) => (
                                    <tr key={order.id}>
                                        <td className="fw-bold">#{order.id}</td>
                                        <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                                        <td>
                                            <Badge bg={getStatusBadgeVariant(order.orderStatus)}>
                                                {order.orderStatus}
                                            </Badge>
                                        </td>
                                        <td>{order.paymentMethod}</td>
                                        <td className="text-end fw-bold">${Number(order.totalAmount).toFixed(2)}</td>
                                        <td className="text-center">
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                onClick={() => handleViewOrder(order.id)}
                                            >
                                                View
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    </Card.Body>
                </Card>
            )}

            {/* Order Details Modal */}
            <Modal show={showModal} onHide={handleCloseModal} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>Order Details</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {detailLoading ? (
                        <div className="text-center py-5">
                            <Spinner animation="border" variant="primary" />
                            <p className="mt-2 text-muted">Loading order details...</p>
                        </div>
                    ) : selectedOrder ? (
                        <>
                            <Row className="mb-4">
                                <Col md={6}>
                                    <h6 className="fw-bold">Order Information</h6>
                                    <p className="mb-1"><strong>Order ID:</strong> #{selectedOrder.id}</p>
                                    <p className="mb-1"><strong>Date:</strong> {new Date(selectedOrder.createdAt).toLocaleString()}</p>
                                    <p className="mb-1">
                                        <strong>Status:</strong>{' '}
                                        <Badge bg={getStatusBadgeVariant(selectedOrder.orderStatus)}>
                                            {selectedOrder.orderStatus}
                                        </Badge>
                                    </p>
                                    <p className="mb-1"><strong>Payment Method:</strong> {selectedOrder.paymentMethod}</p>
                                </Col>
                                <Col md={6}>
                                    <h6 className="fw-bold">Shipping Address</h6>
                                    <p className="mb-0">{selectedOrder.shippingAddressSnapshot}</p>
                                </Col>
                            </Row>

                            <h6 className="fw-bold mb-3">Order Items</h6>
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
                                    {selectedOrder.lineItems && selectedOrder.lineItems.map((item, index) => (
                                        <tr key={index}>
                                            <td>{item.title}</td>
                                            <td className="text-end">${Number(item.unitPrice).toFixed(2)}</td>
                                            <td className="text-center">{item.quantity}</td>
                                            <td className="text-end">${Number(item.subTotal).toFixed(2)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>

                            <div className="d-flex justify-content-end mt-3">
                                <h5 className="fw-bold text-success">
                                    Total: ${Number(selectedOrder.totalAmount).toFixed(2)}
                                </h5>
                            </div>
                        </>
                    ) : (
                        <Alert variant="warning">No order details available</Alert>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleCloseModal}>
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}