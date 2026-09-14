## Bookstore App Backend

Rules
- Implementation follows controller, service, repository layer
- Entity and Dto transformation by mappers using lombok
- Domain Specific Exceptions to be thrown along with message and statusCode backed by enum. 
  Ex: UserAccountException(UserAccountError.DUPLICATE_EMAIL("Email Already Exists",409))
- The error handler should return valid response with status codes
- Appropriate loggers for add to cart, checkout and failures
- The dtos which are accepting should have constraint validation for payloads using @Valid annotation
- The userContextService should be designed with hard-coded value initially to return userContext but implement as thread local.
- Sensistive information should pass as envars. Never hardcode them in code. Dynamic values should come from config maps.
- For every rest controller add proper swagger spec definitions / documentations by converting given raw documentation messages
- Remove step by step raw text instructions given as comments by developer.

## Checkout (Backend)

### Requirement
Cart items checkout flow

### Action Plan 1 : Accept request for checkout 

- The checkout implementation in service layer must use transaction as it involves changes in many tables.
- The checkout request must contain addressId and payment method
- The service layer should fetch cart along with lineItems of userId
- If cart is empty then through exception with message as Cannot checkout an empty shopping cart
- If address is not found by addressId for the given userId through exception with message as Address Not Found
- If payment method not provided during checkout then throw exception Payment method is required for checkout
- Collect address by given addressId and take a snapshot of it.
- Create master order and obtain orderId. Link orderid to lineItems of current order as entity.
- Save line items into the database.
- Clear the items from cart table.
- Return the order response : address, payment methods, total price

### Action Plan 2 : Reduce stock quantity

Below changes needed in service layer

- During checkout flow the books table stock quantity should be reduced and if reduction goes below 0 then it is considered as txn failure. So the update query need to handle it.
- On txn failure due to -ve quantity we should throw exception with proper message OrderException backed by Enum based message and httpCode=409 
- Add test case to justify the stock quanity reduced in correct way
- Add test case to justify the stock quantity went to -ve is treated as txn failure from db
- Add test case After txn failure due to -ve stock quantity, verify cart is not empty and retains previous line items with correct quantity

## Authentication (Backend)

### Requirement
We need user to be register and login. On login success return signed jwt and response authorization header.

### Action Plan 1 : User Registration
- Design database table called USER_ACCOUNT in schema.sql with below columns:
  userId(UUID string unique),email,firstName,lastName,password,blocked(boolean)
  apply index on userId, email
  email must be unique
- Design repository layer in TDD format and using MySQL compatible sql query with methods as saveUserAccount, findByUserId, findByEmail
- Design service layer with validation on UserAccount payload during creation.
  Validation: email must not be registered earlier
  Validation: email, firstName, password are require fields.
  On Validation error we must throw UserAccountException
  The UserAccountException will have required Error object that will have actual message, httpCode. This Error Object can be enum with
  message either can be hardcode or placeholder messages which can be resolved with parameters.
  The UserAccountException should use the httpCode from the Error object and for validation errors we should through 400 bad request.
  Return 409 conflict status code along with UserAccountException when the same email is used for registration
- The service layer should mock the repository layer and implement methods as createUserAccount, findByUserId, findByUserEmail
- UserAccount and UserAccountEntity must be used. They should be convertible to each other using MapStruct mappers.
- UserAccountServiceTest also need to assert the Mappers conversion
- UserAccountController should have endpoint /api/user-accounts. In TDD manner the methods need to be implemented.
  The POST method to create new UserAccount. 
  The GET method to fetch UserAccount by userId


### Action Plan 2 : User Registration Change
- change the endpoint for POST method which creates UserAccount as registration so that the endpoint will become /api/user-accounts/registration
- Implement Bcrypt password conversion before saving the password to database for UserAccount. You may need to add required dependency in pom.xml.
- Verify Bcrypt based password are comparable after save and retrieve from database.
- The controller should not return password as response.

### Action Plan 3 : User login

- For user login we need to add a POST method with /login endpoint in UserAccountController with payload as LoginRequest
- LoginRequest should have email and password which can be delegated to service layer
- Service layer will do validation of mandatory fields email and password with email patterns
- Service layer need to find the user account from repository layer using email.
- If UserAccount not found then through UserAccountException: 404 status code user account not found with email: {}. Also log it properly.
- If UserAccount found then compare the password provided by user and retrieved from database using Bcrypt mechanism
- If password does not match then through UserAccountException: 401 status with message as Login attempt failed. log the message also.
- If password matches then return a token = random UUID to controller
- The UserAccountController must put the token as authorization header in response


### Action Plan 3 : User login with jwt token

- We nned to add jwt dependency and when user login successful the token should be generated a JWT
- So no need to retunr a random token
- The JWT must be signed in nature
- The JWT should have expiry of 3600 seconds, userId, firstName, lastName, email can be present as part of scopes.

### Action Plan 4 : User Authentication Using Jwt Token

The authn only will check does the user belong to our system or not.

The public endpoints which does not authn check are below

BookController: all
HealthController: all
UserAccountController: registration and login

Anything other than this must be protected by authn.

We need a filter which can extend OnecPerUserRequestFilter for authn check.

It should use UserAccountService to find UserAccount by userId.

The UserId can be obtained from the scopes of jwt token. 

The jwt token will come as part of authorization request header.

If token is missing or expired the filter should throw exception with http status as 401.

If token is valid and user is found then set the userId into UserContextService backed by thread local and clear the thread local
in finally block of filter

### Action Plan 5 : UI integration with registration

In React UI we should have Button Registration / Login or Logout.  When clicked it should be shown as a dialog.

Above buttons should be right to Me button.

The dialog or modal can have 2 tabs. Login and Registration.

When login successful the redirect should happen to home page which is at /.

On Login attempt failed it should show the errors in Login tab itself.

On registration attempt failed, it should show the errors in Registration tab itself.

We can think as RegistrationComponent and LoginComponent here. Both of them must implement using js the mandatory field validations like 
we did in backend.

When Login Success the button should convert to Logout. We should store the Authorization header which is a bearer token in browser itself.

When clicked on logout the Authorization Token stroed in browser storage must be removed and redirected to home (/). 

The Logout button should go away and again Registration / Login should be visible. 


### Action Plan 6 : Use Authorization Header for API calls from UI

Use authorization header from browser storage while calling protected endpoints.
So do upfdate the webclient.js. Authorization: Bearer <Token>

### Action Plan 7 : Add to Cart without login should redirect to a Login UI

The home page with / shows all books. It has Add to Cart option. When clicked it should add item to cart.
But adding to a cart needs user login. So whenever user try to add to cart and we see 401 response we should first give 
a alert of bootstrap confirm as (for adding to cart login required) along with a button for link to login. when user clicks login show the 
our exisiting login and registration dialogs.

The Add To Cart is present in home page and also in when we view a single item. We need same behaviour in both the cases.

### Action Plan 8 : Use httpSecure Cookie

Well as we have Authorization header based authn is working, we will use the http secure cookie approach
where on login success the http secure cookie as x-auth-cookie will be set
The browser will send this on subsequent calls to backend.

So that we will get read of headers in filter and from UI (prevents direct javascript access).


## Enahncements

- Implement pagination for list of items in catalog, cart, address, order history
- On checkout page show which items are out of stock in cart wrt to the available stock quantity