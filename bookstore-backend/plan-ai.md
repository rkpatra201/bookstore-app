# Bookstore App Backend

## Development Rules

- **Architecture**: Implementation follows controller, service, and repository layers
- **Data Transformation**: Entity and DTO transformation handled by mappers using Lombok
- **Exception Handling**: Throw domain-specific exceptions with messages and status codes backed by enums
  - Example: `UserAccountException(UserAccountError.DUPLICATE_EMAIL("Email Already Exists", 409))`
- **Error Response**: Error handler must return valid responses with appropriate status codes
- **Logging**: Add appropriate loggers for:
  - Add to cart operations
  - Checkout operations
  - Failure scenarios
- **Validation**: DTOs accepting payloads must have constraint validation using `@Valid` annotation
- **User Context**: UserContextService should be designed with hard-coded values initially but implement as ThreadLocal
- **Security**:
  - Sensitive information must be passed as environment variables - never hardcode in code
  - Dynamic values should come from config maps
- **Documentation**: For every REST controller, add proper Swagger/OpenAPI definitions by converting raw documentation messages
- **Code Cleanup**: Remove step-by-step raw text instructions given as comments by developers

---

## Checkout (Backend)

### Requirement
Implement cart items checkout flow

### Action Plan 1: Accept Request for Checkout

- Checkout implementation in service layer must use **transactions** as it involves changes in multiple tables
- Checkout request must contain:
  - `addressId`
  - Payment method
- Service layer workflow:
  1. Fetch cart along with line items for the userId
  2. **Validation checks:**
     - If cart is empty → throw exception: "Cannot checkout an empty shopping cart"
     - If address is not found by addressId for the given userId → throw exception: "Address Not Found"
     - If payment method is not provided → throw exception: "Payment method is required for checkout"
  3. Collect address by given addressId and take a snapshot of it
  4. Create master order and obtain orderId
  5. Link orderId to line items of current order as entity
  6. Save line items into the database
  7. Clear the items from cart table
  8. Return order response containing:
     - Address
     - Payment method
     - Total price

### Action Plan 2: Reduce Stock Quantity

**Service layer changes:**

- During checkout flow, reduce books table stock quantity
- If reduction goes below 0, treat it as transaction failure
  - Update query must handle this scenario
- On transaction failure due to negative quantity:
  - Throw `OrderException` backed by enum-based message with httpCode=409
- **Test cases required:**
  1. Verify stock quantity is reduced correctly
  2. Verify stock quantity going negative is treated as transaction failure from database
  3. After transaction failure due to negative stock quantity, verify cart is not empty and retains previous line items with correct quantities

---

## Authentication (Backend)

### Requirement
Users must be able to register and login. On successful login, return signed JWT in the Authorization response header.

### Action Plan 1: User Registration

**Database design:**
- Create table `USER_ACCOUNT` in schema.sql with columns:
  - `userId` (UUID string, unique)
  - `email` (unique)
  - `firstName`
  - `lastName`
  - `password`
  - `blocked` (boolean)
- Apply indexes on: `userId`, `email`
- Email must be unique

**Repository layer:**
- Design in TDD format using MySQL-compatible SQL queries
- Methods:
  - `saveUserAccount`
  - `findByUserId`
  - `findByEmail`

**Service layer:**
- Implement validation on UserAccount payload during creation:
  - Email must not be registered earlier
  - Email, firstName, and password are required fields
- **Validation errors:**
  - Throw `UserAccountException` on validation failure
  - UserAccountException contains Error object with actual message and httpCode
  - Error object can be enum with hardcoded or placeholder messages resolved with parameters
  - UserAccountException uses httpCode from Error object
  - Return 400 Bad Request for validation errors
  - Return 409 Conflict when the same email is used for registration
- Mock repository layer in tests
- Implement methods:
  - `createUserAccount`
  - `findByUserId`
  - `findByUserEmail`

**Data models:**
- Use `UserAccount` and `UserAccountEntity`
- Make them convertible to each other using MapStruct mappers
- UserAccountServiceTest must assert mapper conversions

**Controller layer:**
- UserAccountController endpoint: `/api/user-accounts`
- Implement in TDD manner:
  - POST method to create new UserAccount
  - GET method to fetch UserAccount by userId

### Action Plan 2: User Registration Changes

- Change endpoint for POST method that creates UserAccount to `/api/user-accounts/registration`
- Implement BCrypt password conversion before saving password to database
  - Add required dependency in pom.xml
- Verify BCrypt-based passwords are comparable after save and retrieve from database
- Controller must not return password in response

### Action Plan 3: User Login

- Add POST method with `/login` endpoint in UserAccountController
- Payload: `LoginRequest` containing email and password
- **Service layer workflow:**
  1. Validate mandatory fields (email and password) with email pattern validation
  2. Find user account from repository layer using email
  3. **If UserAccount not found:**
     - Throw UserAccountException with 404 status code: "User account not found with email: {}"
     - Log the error
  4. **If UserAccount found:**
     - Compare provided password with database password using BCrypt
  5. **If password does not match:**
     - Throw UserAccountException with 401 status: "Login attempt failed"
     - Log the message
  6. **If password matches:**
     - Return token (random UUID) to controller
- UserAccountController must set the token as Authorization header in response

### Action Plan 4: User Login with JWT Token

- Add JWT dependency
- On successful login, generate JWT token instead of random token
- JWT requirements:
  - Must be signed
  - Expiry: 3600 seconds
  - Include in scopes: userId, firstName, lastName, email

### Action Plan 5: User Authentication Using JWT Token

**Authentication scope:**
- Authentication only checks if the user belongs to the system

**Public endpoints (no authentication check):**
- BookController: all endpoints
- HealthController: all endpoints
- UserAccountController: registration and login only

**Protected endpoints:**
- All endpoints other than those listed above must be protected by authentication

**Implementation:**
- Create filter extending `OncePerRequestFilter` for authentication check
- Filter workflow:
  1. Use UserAccountService to find UserAccount by userId
  2. Extract userId from JWT token scopes
  3. JWT token comes from Authorization request header
  4. **If token is missing or expired:**
     - Throw exception with HTTP status 401
  5. **If token is valid and user is found:**
     - Set userId into UserContextService (backed by ThreadLocal)
     - Clear ThreadLocal in finally block of filter

### Action Plan 6: UI Integration with Registration

**UI Requirements:**
- Add "Registration / Login" or "Logout" button
- Position: Right of "Me" button
- Dialog/modal with 2 tabs: Login and Registration

**Behavior:**
- **On login success:**
  - Redirect to home page at `/`
- **On login attempt failed:**
  - Show errors in Login tab
- **On registration attempt failed:**
  - Show errors in Registration tab

**Implementation:**
- Create RegistrationComponent and LoginComponent
- Both must implement JavaScript-based mandatory field validations (matching backend)
- **On login success:**
  - Button converts to "Logout"
  - Store Authorization header (bearer token) in browser storage
- **On logout click:**
  - Remove Authorization token from browser storage
  - Redirect to home (`/`)
  - Show "Registration / Login" button again

### Action Plan 7: Use Authorization Header for API Calls from UI

- Use Authorization header from browser storage when calling protected endpoints
- Update webclient.js to include: `Authorization: Bearer <Token>`

### Action Plan 8: Add to Cart Without Login Should Redirect to Login UI

**Scenario:**
- Home page (`/`) shows all books with "Add to Cart" option
- Adding to cart requires user login

**Behavior:**
- When user tries to add to cart and receives 401 response:
  1. Show Bootstrap confirm alert: "Login required for adding to cart"
  2. Include button/link to login
  3. When user clicks login, show existing login and registration dialogs
- Apply same behavior in:
  - Home page
  - Single item view page

### Action Plan 9: Use httpOnly Secure Cookie

**Migration from Authorization header to httpOnly cookie:**
- On login success, set httpOnly secure cookie: `x-auth-cookie`
- Browser will send this cookie on subsequent calls to backend
- Remove Authorization header logic from:
  - Filter (backend)
  - UI (frontend)
- **Benefit:** Prevents direct JavaScript access to token (XSS protection)

---

## Enhancements

- Implement pagination for list of items in:
  - Catalog
  - Cart
  - Address
  - Order history
- On checkout page, show which items are out of stock in cart with respect to available stock quantity
