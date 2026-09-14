import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { useCart } from '../../providers/CartProvider';
import { useAuth } from '../../providers/AuthProvider';
import { addToCartInvocation } from '../../client/WebClient';

export function CatalogItemDetails() {
    const location = useLocation();
    const navigate = useNavigate();

    // Connect to the global cart context
    const { setCart } = useCart();
    const { isAuthenticated } = useAuth();

    const addItemToCart = async (item) => {
        // Check authentication before making API call
        if (!isAuthenticated) {
            // Dispatch unauthorized event to show login dialog
            window.dispatchEvent(new CustomEvent('unauthorized', {
                detail: { message: 'Please login to add items to cart.' }
            }));
            return;
        }

        // User is authenticated, proceed with API call
        try {
            const res = await addToCartInvocation(item);
            if (!res.ok) {
                throw new Error(`Failed to add to cart: ${res.status}`);
            }
            const payload = await res.json();

            // Reconcile response data directly - update cart count in navbar
            if (payload.success && payload.data) {
                const totalItems = payload.data.lineItems?.reduce((acc, item) => acc + item.quantity, 0) || 0;
                setCart(totalItems);
            }
        } catch (error) {
            console.error('Error adding to cart:', error);
            // Don't throw - let the 401 handler show login dialog
        }
    }

    const book = location.state?.selectedBook;

    if (!book) {
        return (
            <Container className="my-5 text-center">
                <div className="alert alert-warning border shadow-sm p-4">
                    <h4 className="fw-bold">No Book Details Found</h4>
                    <p className="text-muted">Data context was lost due to a hard page refresh.</p>
                    <Button variant="primary" onClick={() => navigate('/')}>Return to Catalog</Button>
                </div>
            </Container>
        );
    }

    const imageUrl = book.images && book.images.length > 0
        ? book.images[0].url
        : 'https://placeholder.com';

    return (
        <Container className="my-5">
            <Button variant="link" className="text-decoration-none mb-4 p-0 fw-medium" onClick={() => navigate(-1)}>
                ← Back to Catalog
            </Button>

            <Row className="bg-white p-4 rounded border shadow-sm align-items-center">
                <Col md={4} className="text-center mb-4 mb-md-0">
                    <img src={imageUrl} alt={book.title} className="img-fluid rounded border shadow" style={{ maxHeight: '380px', objectFit: 'contain' }} />
                </Col>

                <Col md={8}>
                    <span className="badge bg-secondary mb-2">Book ID: #{book.id}</span>
                    <h2 className="text-dark fw-bold mb-2 text-wrap">{book.title}</h2>
                    <h5 className="text-muted mb-4">Author(s): {book.authors?.map(a => a.authorName).join(', ') || 'Unknown Author'}</h5>
                    <h3 className="text-success fw-bold mb-4">${Number(book.price).toFixed(2)}</h3>

                    <div className="bg-light p-3 rounded mb-4" style={{ maxWidth: '240px' }}>
                        <span className="text-secondary small d-block mb-1">Inventory Management Status:</span>
                        <span className={`fw-bold ${book.stockQty > 0 ? 'text-dark' : 'text-danger'}`}>
                            {book.stockQty > 0 ? `${book.stockQty} items available` : 'Out of stock'}
                        </span>
                    </div>

                    {/* Trigger the context increment state change handler on click */}
                    <Button
                        variant="success"
                        size="lg"
                        className="px-5 fw-bold"
                        disabled={book.stockQty <= 0}
                        onClick={() => addItemToCart(book)}
                    >
                        {book.stockQty > 0 ? 'Add to Shopping Cart' : 'Temporarily Unavailable'}
                    </Button>
                </Col>
            </Row>
        </Container>
    );
}

export default CatalogItemDetails;
