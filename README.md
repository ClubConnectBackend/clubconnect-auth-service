Here’s a comprehensive README file for the **User Microservice**:

---

# User Microservice

The User Microservice is a core component of the ClubConnect application. It provides user authentication, registration, and profile management functionalities. The service interacts with AWS DynamoDB for persistence and integrates with Spring Security for secure access control.

---

## Features

- **User Management**:
  - Register users (regular users and admins).
  - Manage user roles (`ROLE_USER`, `ROLE_ADMIN`).
  - Add or remove events attended by users.
- **Authentication**:
  - JWT-based authentication.
  - Refresh token support.
- **Data Retrieval**:
  - Fetch user details by username.
  - Retrieve attended events for a user.

---

## Tech Stack

- **Frameworks**: Spring Boot, Spring Security
- **Database**: AWS DynamoDB
- **Authentication**: JWT (JSON Web Tokens)
- **Build Tool**: Maven
- **Programming Language**: Java 17

---

## API Endpoints

### Authentication and User Management

| HTTP Method | Endpoint                         | Description                                          |
|-------------|----------------------------------|------------------------------------------------------|
| POST        | `/api/auth/register`            | Register a new user.                                |
| POST        | `/api/auth/register-admin`      | Register a new admin user.                          |
| POST        | `/api/auth/login`               | Authenticate a user and generate a JWT token.       |
| POST        | `/api/auth/refresh-token`       | Generate a new JWT token using a refresh token.     |
| DELETE      | `/api/auth/remove-event/{username}/{eventId}` | Remove an event from a user's attended list. |
| POST        | `/api/auth/add-event/{username}/{eventId}`    | Add an event to a user's attended list.             |
| GET         | `/api/auth/events/{username}`   | Retrieve attended events for a user.                |
| GET         | `/api/auth/email/{username}`    | Retrieve the email address of a user.               |

---

## Architecture

### Layers

1. **Controller Layer**:
   - Contains RESTful API endpoints for handling HTTP requests.
   - Validates and processes user inputs.
   - Returns HTTP responses.

2. **Service Layer**:
   - Implements business logic for user management and authentication.
   - Interacts with repositories for data persistence.
   - Encodes passwords using `PasswordEncoder`.

3. **Repository Layer**:
   - Handles DynamoDB interactions using AWS SDK.
   - Performs CRUD operations for user data.

4. **Security Layer**:
   - Configures JWT authentication.
   - Validates incoming requests with `AuthenticationManager`.

---

## AWS DynamoDB Schema

### Table: **Users**

| Attribute       | Type    | Description                          |
|------------------|---------|--------------------------------------|
| `username`      | String  | Primary key, unique for each user.   |
| `email`         | String  | Email address of the user.           |
| `password`      | String  | Encrypted password.                  |
| `role`          | String  | User's role (e.g., `ROLE_USER`).     |
| `attendedEvents`| List    | List of event IDs attended by user.  |

---

## Security

- **Password Hashing**:
  - Passwords are hashed using Spring Security's `PasswordEncoder`.

- **JWT Token**:
  - Access token is generated upon login and used for subsequent authenticated requests.
  - Refresh tokens are supported to extend session validity.

---

## How to Run the Service

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-repository/user-microservice.git
   cd user-microservice
   ```

2. **Set Up AWS Credentials**:
   Configure your AWS credentials in `~/.aws/credentials` or use environment variables:
   ```bash
   export AWS_ACCESS_KEY_ID=your-access-key
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   ```

3. **Update `application.properties`**:
   - Add your DynamoDB table name and AWS region.
   - Configure JWT signing keys and other security properties.

4. **Build the Project**:
   ```bash
   mvn clean install
   ```

5. **Run the Service**:
   ```bash
   mvn spring-boot:run
   ```

---

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

---

## Example Requests

### Register a New User
**Endpoint**: `/api/auth/register`  
**Method**: `POST`  
**Request Body**:
```json
{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123"
}
```

### Login
**Endpoint**: `/api/auth/login`  
**Method**: `POST`  
**Request Body**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

### Add an Event to a User
**Endpoint**: `/api/auth/add-event/testuser/101`  
**Method**: `POST`

---

## Future Improvements

- Add rate limiting to prevent brute force attacks.
- Implement more comprehensive validation for input data.
- Add multi-factor authentication (MFA).

---

