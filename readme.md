# Library REST API Automation Project

![Library System](Download.png)

This project demonstrates a comprehensive testing framework for a RESTful API that supports Books, Households, Users, and Wishlists. The system has been automated using both **Postman** and **Rest-Assured** frameworks, with rich reporting and continuous integration support.

## Local Testing Execution Example 

[Local Testing Execution Example](Record.gif)

### Books

- **GET** `/books`
  - Retrieve all books.
  
- **GET** `/books/:id`
  - Retrieve a book by its ID.

- **POST** `/books`
  - Create a new book.  
  - **Body**: `{ "title": "Book Title", "author": "Author", "isbn": "ISBN", "releaseDate": "Release Date" }`

- **PUT** `/books/:id`
  - Update a book by its ID.
  - **Body**: `{ "title": "Updated Title", "author": "Updated Author", "isbn": "Updated ISBN", "releaseDate": "Updated Release Date" }`

- **PATCH** `/books/:id`
  - Partially update a book by its ID.
  - **Body**: `{ "title": "Updated Title" }`

- **DELETE** `/books/:id`
  - Delete a book by its ID.

### Households

- **GET** `/households`
  - Retrieve all households.

- **GET** `/households/:id`
  - Retrieve a household by its ID.

- **POST** `/households`
  - Create a new household.
  - **Body**: `{ "name": "Household Name" }`

- **PUT** `/households/:id`
  - Update a household by its ID.
  - **Body**: `{ "name": "Updated Household Name" }`

- **PATCH** `/households/:id`
  - Partially update a household by its ID.
  - **Body**: `{ "name": "Updated Household Name" }`

- **DELETE** `/households/:id`
  - Delete a household by its ID.

### Users

- **GET** `/users`
  - Retrieve all users.

- **GET** `/users/:id`
  - Retrieve a user by its ID.

- **POST** `/users`
  - Create a new user.
  - **Body**: `{ "firstName": "John", "lastName": "Doe", "email": "johndoe@example.com" }`

- **PUT** `/users/:id`
  - Update a user by its ID.
  - **Body**: `{ "firstName": "Updated First Name", "lastName": "Updated Last Name", "email": "updatedemail@example.com" }`

- **PATCH** `/users/:id`
  - Partially update a user by its ID.
  - **Body**: `{ "email": "newemail@example.com" }`

- **DELETE** `/users/:id`
  - Delete a user by its ID.

### Wishlists

- **GET** `/wishlists`
  - Retrieve all wishlists.

- **GET** `/wishlists/:id`
  - Retrieve a wishlist by its ID.

- **POST** `/wishlists`
  - Create a new wishlist.
  - **Body**: `{ "name": "Wishlist Name", "books": [] }`

- **PUT** `/wishlists/:id`
  - Update a wishlist by its ID.
  - **Body**: `{ "name": "Updated Wishlist Name", "books": [] }`

- **PATCH** `/wishlists/:id`
  - Partially update a wishlist by its ID.
  - **Body**: `{ "name": "Updated Wishlist Name" }`

- **DELETE** `/wishlists/:id`
  - Delete a wishlist by its ID.


---

## ✅ Automation Coverage

| Category       | Tool / Framework |
|----------------|------------------|
| Test Clients   | Postman, Rest-Assured (Java) |
| Language       | Java |
| Test Framework | TestNG |
| Build Tool     | Maven (with `pom.xml`) |
| Reporting      | Allure Reports, TestNG Reports, ChainTest |
| CI/CD          | Jenkins |
| Source Control | GitHub |
| IDE            | IntelliJ IDEA |
| Execution CLI  | CMD Batch Commands |

---

## 🧪 Project Testing Strategy

The API automation includes:

- **Postman Collections** with test scripts
- **Rest-Assured TestNG test cases** covering:
  - Positive and Negative scenarios
  - Validation of response time, status codes, headers, data types, and formats
- **JSON Schema validation** (where applicable)
- **Soft Assertions** for grouped validations

---

## 🔄 CI/CD Integration

Jenkins is used for continuous integration. Two approaches are supported:

1. **Freestyle Project**  
   - Batch command to navigate and run Maven:
     ```cmd
     cd /d "D:\Training\Library"
     mvn clean verify -Denv="http://localhost:3000"
     ```

2. **Git Pipeline (Declarative)**  
   Example stage from `Jenkinsfile`:
   ```groovy
   pipeline {
     agent any
     stages {
       stage('Get Code') {
         steps {
           git url: 'https://github.com/your-username/your-repo.git'
         }
       }
       stage('Run Tests') {
         steps {
           bat '''cd /d D:\\Training\\Library && mvn clean verify -Denv="http://localhost:3000"'''
         }
       }
     }
   }


Test Reports:

Allure Reports

Automatically generated using Maven plugin

Can be accessed in Jenkins via Allure plugin

TestNG HTML Reports

Located in target/surefire-reports

ChainTest Reporting

XML-based structured reporting for chaining validations


Structure & Technologies:

API Server: json-server

Request Types: GET, POST, PUT, PATCH, DELETE

Database: db.json or db.test.json for test environments

Environment Variables: Passed via -Denv=...


Directory Overview: 

/src
 └── test/java/testcases
     ├── books
     ├── users
     ├── households
     └── wishlists
/resources
 └── data, schemas, suites
/pom.xml
/testng.xml
/Jenkinsfile


Getting Started:

Clone the repo

Run:
mvn clean install

View Allure reports (if configured):

allure generate --clean
allure open
