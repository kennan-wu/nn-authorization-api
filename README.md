# Spring Boot Neural Labs Authorization API

## Description
This is a RESTful API built with Spring Boot that serves as the authorization server for the Nerual Labs project. It manages sessions with JWTs, and supports Google Oauth and token refreshing. Tokens are sent back through HTTP only cookies to prevent XSS attacks.

## Endpoints
### 1. **POST /auth/signup**
- **Description**: Registers a new user.
- **Request Body**: 
  ```json
  {
    "email": "string",
    "password": "string",
    "username": "string"
  }
  ```
- **Response**: Returns the registered user information.
  
### 2. **POST /auth/login**
- **Description**: Authenticates a user and issues JWT and optionally a refresh token.
- **Request Body**: 
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- **Query Parameter**: 
  - `refresh` (optional): If provided, a refresh token will be included.
- **Response**: Returns the authenticated user's details.
  
### 3. **GET /auth/oauth/authorize**
- **Description**: Redirects the user to an OAuth provider for authentication.
- **Query Parameter**: 
  - `refresh` (optional): If provided, the OAuth request includes refresh token support.
- **Response**: Redirects to the OAuth provider's URI.
  
### 4. **GET /auth/oauth/callback**
- **Description**: Handles the OAuth callback and exchanges the code for tokens.
- **Request Parameters**: 
  - `code`: OAuth authorization code.
  - `state`: OAuth state parameter.
- **Response**: Sets the `id_token` and `refresh_token` cookies, then redirects to the original URL after successful login.

### 5. **POST /auth/refresh**
- **Description**: Refreshes the ID token using the refresh token.
- **Request**: None (uses cookies).
- **Response**: Sets a new `id_token` cookie.

---

### Logout Endpoints

### 6. **POST /logout**
- **Description**: Logs the user out by terminating their session.
- **Request**: None.
- **Response**: A message indicating successful logout.

### 7. **GET /me**
- **Description**: Retrieves the current authenticated user's details.
- **Request**: Requires `id_token` in cookies.
- **Response**: Returns the user's details if the token is valid.

--- 

### Cookie Usage:
- `id_token`: Stores the user's authentication token.
- `refresh_token`: Stores the user's refresh token (optional).

---

### Notes:
- Tokens are stored in HTTP-only cookies for security.
- JWT tokens are used for user authentication, and OAuth is supported for third-party logins.

### Prerequisites
- Java 23.0.1
- Maven
- Redis container running locally on Docker
- MongoDB server
- Google OAuth client

### Setup
1. Clone the repository:
   ```sh
   git clone https://github.com/kennan-wu/nn-authorization-api.git
   cd nn-authorizatioin-api
   ```
2. Create a .env file in the root directory with the variables
   - `MONGO_DB_CONNECTION_STRING`
   - `JWT_SECRET_KEY`
   - `JWT_EXPIRATION`
   - `REFRESH_SECRET_KEY`
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`
   - `GOOGLE_CLIENT_SCOPE`
   - `REDIS_HOST`
   - `REDIS_PORT`
3. Install the dependencies:
   ```
   mvn install
   ```
4. Run the server:
   ```
   mvn spring-boot:run
   ```
   
