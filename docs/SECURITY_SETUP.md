# 🔐 Security Setup Guide

## ⚠️ IMPORTANT: Never Commit Passwords to Git!

This project now uses **environment variables** for all sensitive credentials instead of hardcoded values.

---

## 🎯 Quick Start

### **Option 1: Use the Helper Script (Recommended)**

1. **Edit the credentials** in `SET_ENV_VARS.ps1`:

   ```powershell
   # Open in your editor
   notepad SET_ENV_VARS.ps1

   # Change these lines with your actual passwords:
   $env:POSTGRES_PASSWORD = "your_actual_password"
   $env:RABBITMQ_PASSWORD = "your_actual_password"
   ```

2. **Run the script** (in PowerShell):

   ```powershell
   # The dot (.) is important - it runs in the current session
   . .\SET_ENV_VARS.ps1
   ```

3. **Start your services**:
   ```powershell
   .\scripts\start-all-services.ps1
   ```

### **Option 2: Manual Environment Variables**

Set variables manually in PowerShell:

```powershell
# PostgreSQL
$env:POSTGRES_USERNAME = "postgres"
$env:POSTGRES_PASSWORD = "your_password"

# RabbitMQ
$env:RABBITMQ_USERNAME = "automobile_admin"
$env:RABBITMQ_PASSWORD = "your_password"

# Start services
.\scripts\start-all-services.ps1
```

### **Option 3: Create .env File (For Production)**

1. **Copy the example**:

   ```powershell
   cp .env.example .env
   ```

2. **Edit `.env`** with your actual values:

   ```properties
   POSTGRES_USERNAME=postgres
   POSTGRES_PASSWORD=your_actual_password
   RABBITMQ_USERNAME=automobile_admin
   RABBITMQ_PASSWORD=your_actual_password
   ```

3. **Load variables** (requires additional setup):
   - Use a tool like `dotenv-cli` or
   - Manually load variables before running services

---

## 📋 Environment Variables Reference

### **Required Variables**

| Variable            | Description                  | Default            | Example           |
| ------------------- | ---------------------------- | ------------------ | ----------------- |
| `POSTGRES_PASSWORD` | PostgreSQL database password | `postgres`         | `MySecurePass123` |
| `POSTGRES_USERNAME` | PostgreSQL username          | `postgres`         | `postgres`        |
| `RABBITMQ_PASSWORD` | RabbitMQ password            | `123456`           | `RabbitSecure456` |
| `RABBITMQ_USERNAME` | RabbitMQ username            | `automobile_admin` | `admin`           |

### **Optional Variables**

| Variable                 | Description            | Default                                                 |
| ------------------------ | ---------------------- | ------------------------------------------------------- |
| `SPRING_DATASOURCE_URL`  | Full database JDBC URL | `jdbc:postgresql://localhost:5434/as_user_auth_service` |
| `SPRING_DATA_REDIS_HOST` | Redis host             | `localhost`                                             |
| `SPRING_DATA_REDIS_PORT` | Redis port             | `6379`                                                  |
| `SERVER_PORT`            | User Auth Service port | `8082`                                                  |

---

## 🔧 How It Works

### **application.yml (Backend)**

The `application.yml` file now uses environment variable syntax:

```yaml
spring:
  datasource:
    username: ${POSTGRES_USERNAME:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
```

- `${POSTGRES_PASSWORD:postgres}` means:
  - Use `$env:POSTGRES_PASSWORD` if set
  - Otherwise, use default value `postgres`

### **PowerShell Scripts**

All PowerShell scripts now check for environment variables:

```powershell
# Use environment variable or fallback to default
$pgPassword = if ($env:POSTGRES_PASSWORD) {
    $env:POSTGRES_PASSWORD
} else {
    'postgres'
}
$env:PGPASSWORD = $pgPassword
```

---

## ✅ Verification

Check if variables are set:

```powershell
# Check current session variables
$env:POSTGRES_PASSWORD
$env:RABBITMQ_PASSWORD

# Test database connection
.\scripts\check-all-services.ps1
```

---

## 🚨 Security Best Practices

### **DO:**

✅ Use environment variables for all passwords  
✅ Add `.env` to `.gitignore` (already done)  
✅ Use different passwords for dev/staging/production  
✅ Rotate passwords regularly  
✅ Use strong passwords (12+ characters, mixed case, numbers, symbols)

### **DON'T:**

❌ Commit passwords to Git  
❌ Share passwords in plain text (email, chat, etc.)  
❌ Use default passwords in production  
❌ Reuse passwords across services  
❌ Store passwords in documentation files

---

## 📁 Files Changed

### **Updated Files:**

- ✅ `services/user-auth-service/src/main/resources/application.yml`

  - Changed hardcoded `password: Niro` to `password: ${POSTGRES_PASSWORD:postgres}`
  - Changed hardcoded RabbitMQ password to `${RABBITMQ_PASSWORD:123456}`

- ✅ `scripts/check-all-services.ps1`

  - Now checks `$env:POSTGRES_PASSWORD` before using default

- ✅ `scripts/simple-test.ps1`

  - Now checks `$env:POSTGRES_PASSWORD` before using default

- ✅ `scripts/check-prerequisites.ps1`

  - Now checks `$env:POSTGRES_PASSWORD` before using default

- ✅ `SERVICES_RUNNING.md`
  - Updated instructions to use environment variables

### **New Files:**

- ✅ `.env.example` - Template for environment variables
- ✅ `SET_ENV_VARS.ps1` - Helper script to set variables
- ✅ `SECURITY_SETUP.md` - This guide

### **Files to Update (Manually):**

- 📝 Documentation files mentioning hardcoded passwords
- 📝 Any other scripts not covered above

---

## 🎓 Example Workflow

**Starting the application:**

```powershell
# 1. Set environment variables (one-time per session)
. .\SET_ENV_VARS.ps1

# 2. Verify services are running
.\scripts\check-all-services.ps1

# 3. Start user auth service
.\scripts\start-all-services.ps1

# 4. Test the API
curl http://localhost:8082/actuator/health
```

**Alternative: Inline with commands:**

```powershell
# Set and run in one line
$env:POSTGRES_PASSWORD='MyPassword'; .\mvnw spring-boot:run -pl services/user-auth-service
```

---

## 🔄 Migrating Existing Installations

If you have hardcoded passwords in your local setup:

1. **Find all instances**:

   ```powershell
   # Search for hardcoded passwords
   Select-String -Path .\**\*.yml,.\**\*.ps1 -Pattern "Niro|auth_svc_pass_2024"
   ```

2. **Update each file**:

   - Replace hardcoded values with environment variable syntax
   - Use `${VAR_NAME:default}` in YAML files
   - Use `$env:VAR_NAME` in PowerShell scripts

3. **Test thoroughly**:
   - Verify all services start correctly
   - Check database connections
   - Test all API endpoints

---

## 💾 Production Deployment

### **Docker/Kubernetes:**

```yaml
# docker-compose.yml or Kubernetes Secret
environment:
  - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
  - RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}
```

### **CI/CD (GitHub Actions):**

```yaml
# .github/workflows/deploy.yml
- name: Run tests
  env:
    POSTGRES_PASSWORD: ${{ secrets.POSTGRES_PASSWORD }}
    RABBITMQ_PASSWORD: ${{ secrets.RABBITMQ_PASSWORD }}
  run: mvnw test
```

### **Cloud Platforms:**

- **Azure**: Use Azure Key Vault
- **AWS**: Use AWS Secrets Manager
- **Google Cloud**: Use Secret Manager

---

## ❓ Troubleshooting

### **Problem: "Access denied" errors**

**Solution**: Check if environment variables are set:

```powershell
echo $env:POSTGRES_PASSWORD
```

### **Problem: "Variables not persisting"**

**Solution**: Variables only last for the current session. Run `SET_ENV_VARS.ps1` each time you open a new terminal.

### **Problem: "Still seeing old password"**

**Solution**: Restart your application after setting new environment variables.

---

## 📞 Support

If you have issues:

1. Check this guide first
2. Verify environment variables are set: `echo $env:POSTGRES_PASSWORD`
3. Check application logs for connection errors
4. Ensure `.env` file is not committed to Git

---

**✅ Your credentials are now secure!** 🔒
