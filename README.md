# EdgeFlow Gateway

A production-inspired API Gateway built with Java 21, Spring Boot, and Spring Cloud Gateway.

EdgeFlow demonstrates authentication, role-based authorization, distributed rate limiting, load balancing, request tracing, centralized routing, and observability in a containerized microservices environment.

## Features

- API Gateway with Spring Cloud Gateway
- JWT Authentication with Keycloak
- Role-Based Access Control (RBAC)
- Redis-backed distributed Rate Limiting
- Client-side Load Balancing
- Multiple User Service instances
- Request Correlation IDs
- Request/Response transformation
- Centralized request logging
- Prometheus metrics
- Pre-provisioned Grafana dashboard
- Docker Compose environment

## Architecture

```text
                         ┌──────────────┐
                         │    Client    │
                         └──────┬───────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │    EdgeFlow Gateway   │
                    │                       │
                    │ JWT Authentication    │
                    │ RBAC                  │
                    │ Rate Limiting         │
                    │ Correlation ID        │
                    │ Load Balancing        │
                    └───────┬───────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
        ┌───────────────┐       ┌───────────────┐
        │ User Service  │       │Payment Service│
        │               │       │               │
        │   user-1      │       └───────────────┘
        │   user-2      │
        └───────────────┘

             Supporting Infrastructure

        ┌──────────┐   ┌────────────┐
        │ Keycloak │   │   Redis    │
        └──────────┘   └────────────┘

        ┌────────────┐ ┌────────────┐
        │ Prometheus │ │  Grafana   │
        └────────────┘ └────────────┘

Technology Stack
Technology	Purpose
Java 21	Application runtime
Spring Boot 3.4	Application framework
Spring Cloud Gateway	API Gateway
Spring Cloud LoadBalancer	Client-side load balancing
Spring Security	Security integration
Keycloak	Identity and access management
OAuth2 / JWT	Authentication
Redis	Distributed rate limiting
Micrometer	Application metrics
Prometheus	Metrics collection
Grafana	Monitoring dashboards
Docker Compose	Local infrastructure orchestration
Services
Service	Host Port
Gateway	18080
User Service #1	18081
Payment Service	18082
User Service #2	18083
Keycloak	18084
Redis	18085
Prometheus	18086
Grafana	18087
Build

Build all application modules:

mvn clean package -DskipTests
Run

Start the complete environment:

docker compose up -d --build

Check running containers:

docker compose ps
Authentication

EdgeFlow uses Keycloak as its OpenID Connect identity provider.

For the local demo environment:

Realm: edgeflow
Client: edgeflow-gateway

Obtain an access token:

TOKEN=$(curl -s -X POST \
  http://localhost:18084/realms/edgeflow/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=edgeflow-gateway" \
  -d "client_secret=edgeflow-demo-secret" \
  -d "username=arman" \
  -d "password=123" \
  -d "grant_type=password" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

The credentials above are local demo credentials only and must not be used in production.

Authentication Test

Calling a protected endpoint without a JWT:

curl -i http://localhost:18080/api/users/1

Returns:

HTTP/1.1 401 Unauthorized

With a valid JWT:

curl -i \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:18080/api/users/1
Role-Based Authorization

The gateway maps Keycloak client roles to Spring Security authorities.

Available demo roles:

USER
PAYMENT
ADMIN

User endpoints require:

USER or ADMIN

Payment endpoints require:

PAYMENT or ADMIN

Example:

curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:18080/api/payments
Rate Limiting

User routes use Redis-backed distributed rate limiting.

Current demo configuration:

Replenish Rate: 2 requests/second
Burst Capacity: 2
Requested Tokens: 1

The gateway returns:

HTTP 429 Too Many Requests

when the limit is exceeded.

Rate limit headers are also returned:

X-RateLimit-Remaining
X-RateLimit-Burst-Capacity
X-RateLimit-Replenish-Rate
Load Balancing

The User Service runs as two independent instances:

user-1
user-2

The gateway routes requests through:

lb://user-service

Spring Cloud LoadBalancer distributes requests between both instances.

Test:

for i in {1..6}; do
  curl -s \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:18080/api/users/1
  echo
  sleep 1
done

Example output:

{"instance":"user-2","service":"user-service","name":"Arman","id":1}
{"instance":"user-1","service":"user-service","name":"Arman","id":1}
{"instance":"user-2","service":"user-service","name":"Arman","id":1}
{"instance":"user-1","service":"user-service","name":"Arman","id":1}
Request Correlation

Every request receives an X-Correlation-Id.

If the client provides one, EdgeFlow preserves it. Otherwise, the gateway generates a UUID.

Example response headers:

X-Correlation-Id: ea08a968-8849-4b00-9dd3-491758128c32
X-EdgeFlow-Gateway: EdgeFlow

The same correlation ID is included in gateway request/response logs.

Observability

Spring Boot Actuator and Micrometer expose application metrics.

Gateway Prometheus endpoint:

http://localhost:18080/actuator/prometheus

Prometheus:

http://localhost:18086

Grafana:

http://localhost:18087

Default local Grafana credentials:

admin / admin
Grafana Dashboard

The repository automatically provisions the EdgeFlow Gateway dashboard.

Included panels:

Gateway Request Throughput
Gateway CPU Usage
JVM Heap Memory Usage
Live JVM Threads
Active Gateway Routes

The Prometheus datasource and dashboard are provisioned automatically when Grafana starts.

Project Structure
edgeflow-gateway/
├── gateway-service/
├── user-service/
├── payment-service/
├── infra/
│   └── keycloak/
├── monitoring/
│   ├── prometheus.yml
│   └── grafana/
│       ├── dashboards/
│       └── provisioning/
├── docker-compose.yml
├── pom.xml
└── README.md
Security Note

This repository is designed as a local demonstration and portfolio project.

The included Keycloak credentials and client secret are demo-only values. Production deployments should use external secret management, TLS, persistent identity storage, hardened Keycloak configuration, and production-grade infrastructure.

Author

Arman Sadeghian

Backend Developer
Java • Spring Boot • Microservices