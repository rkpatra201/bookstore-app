import { useEffect, useState } from "react"
import { CATALOG_URL, HEALTH_URL } from "../../constants/AppConstants";

import Button from 'react-bootstrap/Button';
import Card from 'react-bootstrap/Card';
import { Container, Image } from "react-bootstrap";
import { Row, Col } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useCart } from "../../providers/CartProvider";
import { addToCartInvocation } from "../../client/WebClient";
export function Catalog() {

    const [books, setBooks] = useState([]);

    function loadData() {
        fetch(CATALOG_URL)
            .then(res => res.json())
            .then(json => {
                console.log(json);
                setBooks((prev) => json.data);
            })
    }

    useEffect(() => {
        loadData();
    }, []);

    return <>
        <h1>Catalog</h1>
        <BookGrid books={books} />
    </>

}

function BookGrid({ books }) {
    const navigate = useNavigate();
    const {incrementCart} = useCart();

    const addItemToCart = (item)=>{
        addToCartInvocation(item)
        .then(res=>res.json())
        .then(json=>incrementCart(item))
    }

    return (
        <Container className="my-4">
            <Row>
                {books.map((item) => (
                    // 1. Wrap each card in a responsive Column
                    <Col key={item.id} xs={12} sm={6} md={4} lg={3} className="mb-4">

                        {/* 2. Force equal card heights using h-100 and flex direction */}
                        <Card className="h-100 d-flex flex-column shadow-sm">

                            {/* Move image outside Card.Body so it snaps perfectly to the top edge */}
                            <Card.Img
                                variant="top"
                                src={item.images?.[0]?.url}
                                alt={item.title}
                                style={{ height: '240px', objectFit: 'cover' }}
                            />

                            {/* 3. Turn Card.Body into a vertical flex container */}
                            <Card.Body className="d-flex flex-column p-3">
                                <Card.Title className="fs-6 fw-bold mb-2 text-wrap">
                                    {item.title}
                                </Card.Title>

                                <Card.Text className="fw-bold text-success mb-3">
                                    ${Number(item.price).toFixed(2)}
                                </Card.Text>

                                {/* 4. mt-auto forces this button to align perfectly at the bottom */}
                                <div className="d-flex gap-2 mt-auto">
                                    <Button
                                        variant="outline-primary"
                                        className="w-50 fw-medium btn-sm"
                                         onClick={() => {
                        // Triggers programmatic routing and binds the exact item object into history state context
                        navigate(`/book/${item.id}`, { state: { selectedBook: item } });
                      }}
                                        >
                                        View
                                    </Button>
                                    <Button
                                        variant="primary"
                                        className="w-50 fw-medium btn-sm"
                                        disabled={item.stockQty <= 0}
                                        onClick={()=>addItemToCart(item)}
                                    >
                                        {item.stockQty > 0 ? 'Add' : 'Out Of Stock'}
                                    </Button>
                                </div>
                            </Card.Body>

                        </Card>
                    </Col>
                ))}
            </Row>
        </Container>
    );
}