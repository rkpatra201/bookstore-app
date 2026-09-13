
import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Card, Alert } from 'react-bootstrap';
import { addressInvocation } from '../../client/WebClient';
export function AddressComponent() {

    const [errors, setErrors] = useState({});
    const [refreshList, serRefreshList] = useState(true);

    // 3. Centralised inputs change listener handler
    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));

        // Clear error message dynamically for this field as the customer types
        if (errors[name]) {
            setErrors((prev) => ({ ...prev, [name]: '' }));
        }
    };

    // 1. Core form state matching your CustomerAddress DTO properties
    const [formData, setFormData] = useState({
        alias: 'Home',
        recipientName: 'John Doe',
        addressLine1: 'Line 1',
        addressLine2: 'Line 2',
        city: 'BLR',
        state: 'KA',
        postalCode: '560067',
        country: 'INDIA',
        phoneNumber: '',
        isDefault: false
    });

    const control = {
        phoneNumber: {
            label: 'Phone Number',
            placeHolder: '+91 8971 647 098',
            data: formData.phoneNumber,
            name: 'phoneNumber',
            inputChange: handleInputChange,
            errorText: errors.phoneNumber
        },
        country: {
            label: 'country',
            placeHolder: 'INDIA',
            data: formData.country,
            name: 'country',
            inputChange: handleInputChange,
            errorText: errors.country
        },
        postalCode: {
            label: 'Postal Code',
            placeHolder: '560067',
            data: formData.postalCode,
            name: 'postalCode',
            inputChange: handleInputChange,
            errorText: errors.postalCode
        },
        state: {
            label: 'State',
            placeHolder: 'Karnataka',
            data: formData.state,
            name: 'state',
            inputChange: handleInputChange,
            errorText: errors.state
        },
        city: {
            label: 'City',
            placeHolder: 'Bengaluru',
            data: formData.city,
            name: 'city',
            inputChange: handleInputChange,
            errorText: errors.city
        },
        addressLine2: {
            label: 'Address Line 2',
            placeHolder: '80 Feet Road',
            data: formData.addressLine2,
            name: 'addressLine2',
            inputChange: handleInputChange,
            errorText: errors.addressLine2
        },
        addressLine1: {
            label: 'Address Line 1',
            placeHolder: 'Whitefield',
            data: formData.addressLine1,
            name: 'addressLine1',
            inputChange: handleInputChange,
            errorText: errors.addressLine1

        },
        recipientName: {
            label: 'Reciepient Name',
            placeHolder: 'John Doe',
            data: formData.recipientName,
            name: 'recipientName',
            inputChange: handleInputChange,
            errorText: errors.recipientName

        },
        alias: {
            label: 'Address Alias',
            placeHolder: 'My Home Address',
            data: formData.alias,
            name: 'alias',
            inputChange: handleInputChange,
            errorText: errors.alias

        }
    }

    // 2. State to hold tracking validation error message mappings
    const [serverMessage, setServerMessage] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 4. Custom validation logic framework layer
    const validateForm = () => {
        const newErrors = {};

        if (!formData.alias.trim()) newErrors.alias = 'Address nickname is required (e.g. Home, Work).';
        if (!formData.recipientName.trim()) newErrors.recipientName = 'Recipient name is required.';
        if (!formData.addressLine1.trim()) newErrors.addressLine1 = 'Street address line 1 is required.';
        if (!formData.city.trim()) newErrors.city = 'City is required.';
        if (!formData.state.trim()) newErrors.state = 'State is required.';

        // Postal code validation bounds rule checking parameter
        if (!formData.postalCode.trim()) {
            newErrors.postalCode = 'Postal code is required.';
        } else if (!/^\d{5,6}$/.test(formData.postalCode)) {
            newErrors.postalCode = 'Postal code must be a valid 5 or 6 digit number.';
        }

        if (!formData.country.trim()) newErrors.country = 'Country is required.';

        // International phone number validation standard logic regex check
        if (!formData.phoneNumber.trim()) {
            newErrors.phoneNumber = 'Phone number is required.';
        }

        setErrors(newErrors);
        // Returns true only if the errors map remains completely empty
        return Object.keys(newErrors).length === 0;
    };

    // 5. Native Form submission dispatcher pipeline handler
    const handleSubmit = async (e) => {
        console.log('Form Submit : Save Address')
        e.preventDefault(); // Stop default browser full-page reload triggers
        setServerMessage(null);

        // Validate parameters locally before dispatching HTTP network requests
        if (!validateForm()) {
            console.log('Form validation errors: ', errors)
            return
        };

        setIsSubmitting(true);

        try {
            const response = await addressInvocation().saveAddress(formData);
            const payload = await response.json();

            if (response.ok && payload.success) {
                setServerMessage({ type: 'success', text: 'Address saved successfully!' });
                // Reset inputs back to clean structural states maps dynamically
                // setFormData({
                //     alias: '', recipientName: '', addressLine1: '', addressLine2: '',
                //     city: '', state: '', postalCode: '', country: '', phoneNumber: '', isDefault: false
                // });
                serRefreshList(!refreshList);
            } else {
                throw new Error(payload.message || 'Server rejected address validation mapping parameters.');
            }
        } catch (error) {
            setServerMessage({ type: 'danger', text: error.message });
        } finally {
            setIsSubmitting(false);
            setTimeout(() => {
                setServerMessage(null);
            }, 5000);
        }
    };


    return <>
        <Card className="border-0 bg-transparent">
            <Card.Body className="p-0">
                <h4 className="fw-bold mb-4 text-dark">🏡 Add New Shipping Address</h4>

                {serverMessage && (
                    <Alert variant={serverMessage.type} className="mb-4 shadow-sm">
                        {serverMessage.text}
                    </Alert>
                )}

                {JSON.stringify(formData)}
                {JSON.stringify(errors)}
                <Form onSubmit={handleSubmit}>

                    <TextFormControlObject control={control.alias} />
                    <TextFormControlObject control={control.recipientName} />
                    <TextFormControlObject control={control.addressLine1} />
                    <TextFormControlObject control={control.addressLine2} />
                    <TextFormControlObject control={control.city} />
                    <TextFormControlObject control={control.state} />
                    <TextFormControlObject control={control.postalCode} />
                    <TextFormControlObject control={control.country} />
                    <TextFormControlObject control={control.phoneNumber} />

                    <Button variant="success" type="submit" className="fw-bold px-5">
                        Save Address
                    </Button>
                </Form>
            </Card.Body>
        </Card>
        <AddressListComponent refreshTrigger={refreshList} enableDelete={true} />
    </>
}

function TextFormControlObject({ control }) {
    return <>
        <TextFormControl
            label={control.label}
            name={control.name}
            data={control.data}
            inputChange={control.inputChange}
            placeHolder={control.placeHolder}
            errorText={control.errorText}
        />
    </>
}

function TextFormControl(control) {
    return (
        <Form.Group className="mb-3" controlId={control.name}>
            <Form.Label className="small fw-semibold text-muted">{control.label}</Form.Label>
            {control.errorText &&
                <Form.Label className="small fw-semibold text-danger">&nbsp;{control.errorText} </Form.Label>
            }

            <Form.Control
                type="text"
                name={control.name}
                value={control.data}
                onChange={control.inputChange}
                placeholder={control.placeHolder}
            />
        </Form.Group>
    );
}



export function AddressListComponent({ refreshTrigger, enableDelete, addressSelectionHandler }) {
    const [addresses, setAddresses] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [localRefresh, setLocalRefresh] = useState(true);

    const fetchAddresses = async () => {
        try {
            setIsLoading(true);
            const response = await addressInvocation().list();
            if (response.ok) {
                const json = await response.json();
                const data = json.data;
                setAddresses(Array.isArray(data) ? data : data.addresses || []);
            }
        } catch (error) {
            console.error("Failed to load addresses:", error);
        } finally {
            setIsLoading(false);
        }
    };

    // 🔄 Reacts to the parent state flipping trigger switches
    useEffect(() => {
        fetchAddresses();
    }, [refreshTrigger, localRefresh]); // Adding refreshTrigger as a dependency makes this execute automatically on updates

    if (isLoading && addresses.length === 0) {
        return <p className="text-muted small">Loading saved addresses...</p>;
    }

    function deleteAddress(addr) {
        addressInvocation()
            .deletAddress(addr.id)
            .then(res => res.json())
            .then(json => {
                setLocalRefresh(!localRefresh);
            })
    }
    return (
        <>
            <h4 className="fw-bold mb-4 text-dark">📋 Saved Shipping Addresses</h4>
            {addresses.length === 0 ? (
                <p className="text-muted small">No saved addresses found. Add one on the left!</p>
            ) : (
                <Row className="g-3">
                    {addresses.map((addr, index) => (
                        <Col md={6} key={addr.id || index}>
                            <AddressCardComponent enableDelete={enableDelete}
                                addr={addr}
                                addressSelectionHandler={addressSelectionHandler}
                                deleteAddress={deleteAddress} />
                        </Col>
                    ))}
                </Row>
            )}
        </>
    );
}

export function AddressCardComponent({ addr, enableDelete, addressSelectionHandler, deleteAddress }) {
    return <>
        <Card className="h-100 border shadow-sm">
            <Card.Body className="d-flex flex-column justify-content-between">
                <div>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                        <span className="badge bg-secondary px-2.5 py-1.5">{addr.alias}</span>
                        {addr.isDefault && <span className="badge bg-primary">Default</span>}
                    </div>

                    {enableDelete && <span className="badge bg-primary" onClick={() => deleteAddress(addr)}>Delete</span>}
                    {addressSelectionHandler && <span className="badge bg-primary" onClick={() => addressSelectionHandler(addr)}>Select Shipping Address</span>}
                    <h6 className="fw-bold text-dark mb-1">{addr.recipientName}</h6>
                    <p className="text-muted small mb-1 lh-sm">
                        {addr.addressLine1},<br />
                        {addr.addressLine2 && <>{addr.addressLine2},<br /></>}
                        {addr.city}, {addr.state} - {addr.postalCode}<br />
                        {addr.country}
                    </p>
                </div>
                <div className="border-top pt-2 mt-2 small text-muted">
                    📞 {addr.phoneNumber}
                </div>
            </Card.Body>
        </Card>
    </>
}


