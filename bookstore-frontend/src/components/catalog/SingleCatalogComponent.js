import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Button, Card } from 'react-bootstrap';

export function CatalogItemDetails() {
  const location = useLocation();
  const navigate = useNavigate();

  // HOW TO RECEIVE THE STATE: Extract the object you attached in navigate() 
  const book = location.state?.selectedBook;

  // Fallback Rule: If the user refreshed the page or typed the URL manually, state will be missing
  if (!book) {
    return (
      <Container className="my-5 text-center">
        <div className="alert alert-warning border shadow-sm p-4">
          <h4 className="fw-bold">No Book Details Found</h4>
          <p className="text-muted">Data context was lost due to a hard page refresh or direct URL input entry mapping paths.</p>
          <Button variant="primary" onClick={() => navigate('/')}>
            Return to Catalog
          </Button>
        </div>
      </Container>
    );
  }

  const imageUrl = book.images && book.images.length > 0 
    ? book.images[0].url 
    : 'https://placeholder.com';

  return (
    <Container className="my-5">
      {/* Dynamic Browser Back Navigation using history stack indices (-1) */}
      <Button variant="link" className="text-decoration-none mb-4 p-0 fw-medium" onClick={() => navigate(-1)}>
        ← Back to Catalog
      </Button>
      
      <Row className="bg-white p-4 rounded border shadow-sm align-items-center">
        {/* Left Side: Product Media Layout Screen Banner Image */}
        <Col md={4} className="text-center mb-4 mb-md-0">
          <img 
            src={imageUrl} 
            alt={book.title} 
            className="img-fluid rounded border shadow"
            style={{ maxHeight: '380px', objectFit: 'contain' }}
          />
        </Col>
        
        {/* Right Side: Product Information Details Nodes */}
        <Col md={8}>
          <span className="badge bg-secondary mb-2">Book ID: #{book.id}</span>
          <h2 className="text-dark fw-bold mb-2 text-wrap">{book.title}</h2>
          
          <h5 className="text-muted mb-4">
            Author(s): {book.authors?.map(a => a.authorName).join(', ') || 'Unknown Author'}
          </h5>
          
          <h3 className="text-success fw-bold mb-4">${Number(book.price).toFixed(2)}</h3>
          
          <div className="bg-light p-3 rounded mb-4" style={{ maxWidth: '240px' }}>
            <span className="text-secondary small d-block mb-1">Inventory Management Status:</span>
            <span className={`fw-bold ${book.stockQty > 0 ? 'text-dark' : 'text-danger'}`}>
              {book.stockQty > 0 ? `${book.stockQty} items available` : 'Out of stock'}
            </span>
          </div>

          <Button 
            variant="success" 
            size="lg" 
            className="px-5 fw-bold"
            disabled={book.stockQty <= 0}
          >
            {book.stockQty > 0 ? 'Add to Shopping Cart' : 'Temporarily Unavailable'}
          </Button>
        </Col>
      </Row>
    </Container>
  );
}

export default CatalogItemDetails;
