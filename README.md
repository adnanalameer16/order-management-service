# Order Management Service
Order Management Service is an event-driven backend system built with Spring Boot, Apache Kafka, and Aerospike. It handles high-volume order processing, asynchronous payment flows, and distributed status updates.
---
## Architecture Flow
### Order Creation Flow
```
┌─────────┐      POST /orders       ┌─────────────────┐
│  Client │ ───────────────────────> │ Order Service   │
└─────────┘                          └────────┬────────┘
                                              │
                                              │ 1. Save Order
                                              ▼
                                     ┌──────────────────┐
                                     │   Aerospike DB   │
                                     └──────────────────┘
                                              │
                                              │ 2. Produce Event
                                              ▼
                                     ┌──────────────────┐
                                     │  Kafka Topic:    │
                                     │  order.created   │
                                     └──────────────────┘
```
### Payment & Shipping Flow
```
┌─────────┐   POST /payments/complete   ┌──────────────────┐
│  Client │ ──────────────────────────> │ Payment Service  │
└─────────┘                             └────────┬─────────┘
                                                 │
                                                 │ 1. Validate Order
                                                 ▼
                                        ┌──────────────────┐
                                        │   Aerospike DB   │
                                        └──────────────────┘
                                                 │
                                                 │ 2. Produce Event
                                                 ▼
                                        ┌──────────────────────┐
                                        │  Kafka Topic:        │
                                        │  payment.completed   │
                                        └──────────┬───────────┘
                                                   │
                                                   │ 3. Consume Event
                                                   ▼
                                        ┌──────────────────────┐
                                        │  Order Consumer      │
                                        │  (Update to PAID)    │
                                        └──────────┬───────────┘
                                                   │
                                                   │ 4. Produce Event
                                                   ▼
                                        ┌───────────────────────────┐
                                        │  Kafka Topic:             │
                                        │  order.ready_for_shipping │
                                        └───────────────────────────┘
```
---
## Prerequisites
Before running the service, ensure you have:
- **Java 17+**
- **Docker** or **Podman** (for infrastructure)
- **Maven 3.8+** (or use the included Maven wrapper)
---
## Local Setup
### **Step 1: Start Infrastructure**
Launch Kafka and Aerospike using Podman Compose (or Docker Compose):
```bash
podman-compose up -d
```
*Services:* Kafka (9092), Aerospike (3000)
### **Step 2: Run the Application**
```bash
./mvnw clean spring-boot:run
```
The service will start at **`http://localhost:8080`**.
### **Step 3: View API Documentation (Swagger)**
Once running, explore and test APIs interactively:
👉 **http://localhost:8080/swagger-ui/index.html**
---
## API Reference (Curl Examples)
You can test the endpoints directly using these commands.
### **1. Create an Order**
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: test-trace-1" \
  -d '{
    "customerId": "cust-001",
    "orderItems": [
      { "productId": "laptop", "price": 1000.0, "quantity": 1 },
      { "productId": "mouse", "price": 50.0, "quantity": 2 }
    ]
  }'
```

**Expected Response (201 Created):**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "cust-001",
  "orderItems": [
    {
      "productId": "laptop",
      "productName": "laptop",
      "quantity": 1,
      "price": 1000.0
    },
    {
      "productId": "mouse",
      "productName": "mouse",
      "quantity": 2,
      "price": 50.0
    }
  ],
  "subtotal": 1100.0,
  "taxAmount": 110.0,
  "totalAmount": 1210.0,
  "status": "PENDING",
  "createdAt": "2026-01-20T15:30:45.123Z"
}
```

### **2. Get Order Details**
```bash
curl -X GET http://localhost:8080/orders/{ORDER_ID_FROM_STEP_1}
```

**Expected Response (200 OK):**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "cust-001",
  "orderItems": [
    {
      "productId": "laptop",
      "productName": "laptop",
      "quantity": 1,
      "price": 1000.0
    },
    {
      "productId": "mouse",
      "productName": "mouse",
      "quantity": 2,
      "price": 50.0
    }
  ],
  "subtotal": 1100.0,
  "taxAmount": 110.0,
  "totalAmount": 1210.0,
  "status": "PENDING",
  "createdAt": "2026-01-20T15:30:45.123Z",
  "updatedAt": "2026-01-20T15:30:45.123Z"
}
```

### **3. List Customer Orders (Pagination)**
```bash
curl -X GET "http://localhost:8080/customers/cust-001/orders?page=0&size=5"
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "orderId": "550e8400-e29b-41d4-a716-446655440000",
      "customerId": "cust-001",
      "totalAmount": 1210.0,
      "status": "PENDING",
      "createdAt": "2026-01-20T15:30:45.123Z"
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

### **4. Process Payment**
Triggers the async Kafka flow to update status to `PAID`.

```bash
curl -X POST http://localhost:8080/payments/complete \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: test-trace-1" \
  -d '{
    "orderId": "{ORDER_ID_FROM_STEP_1}",
    "paymentId": "pay-98765"
  }'
```

**Expected Response (200 OK):**
```json
{
  "message": "Payment processed successfully",
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentId": "pay-98765",
  "status": "PAID"
}
```
---
## Event Schemas (Kafka)
| Topic Name | Payload Example |
| --- | --- |
| `order.created` | `{ "eventType": "order.created", "orderId": "...", "totalAmount": 1100.0, "correlationId": "..." }` |
| `payment.completed` | `{ "orderId": "...", "paymentId": "pay-1", "correlationId": "..." }` |
| `order.ready_for_shipping` | `{ "orderId": "...", "correlationId": "..." }` |
---
## Configuration
| Profile | File | Tax Rate | Command |
| --- | --- | --- | --- |
| **Dev** (Default) | `application-dev.properties` | 10% | `./mvnw spring-boot:run` |
| **Prod** | `application-prod.properties` | 20% | `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod` |
---
## Reflection
**Improvements with more time:**
If more time were available, I would implement **Spring Security (OAuth2)** to secure the endpoints, as currently, the API is open. I would also add **Integration Tests using Testcontainers** to validate the Aerospike and Kafka interactions in a real Docker environment, rather than relying solely on mocks.