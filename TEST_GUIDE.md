# VitaSync Auth Service Testing

## ✅ Simple Testing with Spring Boot

### 1. Run Tests (Recommended)
```bash
./gradlew test
```
**Status: ✅ ALL TESTS PASSING**

### 2. Start Application (for manual testing)
```bash
./gradlew bootRun
```

### 3. Quick Manual Tests (while app is running)

#### Health Check
```bash
curl http://localhost:8080/auth/health
```

#### Service Info  
```bash
curl http://localhost:8080/auth/info
```

#### Register User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@vitasync.com",
    "password": "SecurePass123!",
    "firstName": "Test",
    "lastName": "User", 
    "phoneNumber": "+1234567890",
    "role": "PATIENT",
    "bloodType": "A+"
  }'
```

#### Login User
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@vitasync.com",
    "password": "SecurePass123!"
  }'
```

## ✅ Test Summary
- ✅ Health endpoint works
- ✅ Service info endpoint works  
- ✅ Error handling works
- ✅ JWT token validation works
- ✅ All endpoints respond correctly
- ✅ Security configuration is correct

## 🚀 Ready for Microservice Integration
Your auth service is production-ready and can be used by other microservices via:
- `/auth/validate` - Token validation
- `/auth/user/{userId}` - Get user by ID
- `/auth/health` - Health checks
