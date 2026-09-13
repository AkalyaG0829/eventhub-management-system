# EventHub Production Deployment Guide

This document contains the final readiness report and the step-by-step instructions for deploying EventHub to production.

## 1. Readiness Audit & Changes Summary

The project has been fully audited and configured for production deployment using environment variables, avoiding hardcoded dependencies and local URLs.

### Files Modified

1.  **`backend/src/main/resources/application.yml`**
    *   Replaced hardcoded `localhost` database URL with environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
    *   Set `server.port` to use the `${PORT:8080}` variable required by Render.
    *   Added a fallback default for `JWT_SECRET`.
2.  **`backend/src/main/java/com/eventhub/security/SecurityConfig.java`**
    *   Injected `${FRONTEND_URL}` into CORS configuration to allow requests from the deployed Vercel frontend.
3.  **`backend/src/main/java/com/eventhub/controller/RegistrationController.java`**
    *   Disabled the `/test-payment` API endpoint in production by gating it behind an `ENABLE_TEST_PAYMENT` environment variable.
4.  **`frontend/src/utils/axiosConfig.js`**
    *   Removed the `http://localhost:8080` fallback. It now strictly uses `import.meta.env.VITE_API_BASE_URL`.
5.  **`frontend/.env.example` & `backend/.env.example`**
    *   Created placeholder templates to safely document the environment variables required.
6.  **`frontend/vercel.json`**
    *   Added a URL rewrite configuration so React Router SPA navigation works correctly on Vercel without returning 404 errors on refresh.

### Build & Test Results
*   **Backend Tests**: PASSED (`mvnw clean test` succeeded).
*   **Backend Build**: PASSED (`mvnw clean package` built successfully).
*   **Frontend Build**: PASSED (`npm run build` completed in ~16s).

### Important Confirmations
*   **No Test Data Migration**: The production database will start empty. Spring Boot's `ddl-auto=update` will generate the schema automatically on the first run.
*   **Payment Endpoint Protected**: The test payment endpoint will reject requests in production because `ENABLE_TEST_PAYMENT` is not set.

---

## 2. Required Environment Variables

### Aiven MySQL Database
You will get these from Aiven after creating your MySQL service:
*   **Host**: e.g., `mysql-eventhub...aivencloud.com`
*   **Port**: e.g., `12345`
*   **Username**: `avnadmin` (or similar)
*   **Password**: `<your-aiven-password>`
*   **Database Name**: `defaultdb` (or `eventhub`)

### Render Backend Service Variables
Add these environment variables when configuring your Render Web Service:
*   `DB_URL` = `jdbc:mysql://<aiven-host>:<aiven-port>/defaultdb?useSSL=true&serverTimezone=UTC`
*   `DB_USERNAME` = `<aiven-username>`
*   `DB_PASSWORD` = `<aiven-password>`
*   `JWT_SECRET` = `(Generate a secure 32+ character string)`
*   `FRONTEND_URL` = `https://<your-vercel-project-url>.vercel.app`

### Vercel Frontend Variables
Add this environment variable when importing your project in Vercel:
*   `VITE_API_BASE_URL` = `https://<your-render-app-name>.onrender.com`

---

## 3. Step-by-Step Deployment Instructions

### Step 1: Create Database (Aiven)
1.  Sign up for Aiven and create a **Free MySQL** service.
2.  Wait for the service to start.
3.  Go to the **Overview** tab and copy your Connection parameters (Host, Port, User, Password).

### Step 2: Deploy Backend (Render)
1.  Push all your current code changes to a GitHub repository.
2.  Sign up for **Render.com** and click **New+** -> **Web Service**.
3.  Connect your GitHub repository.
4.  **Root Directory**: Leave blank or type `backend`.
5.  **Environment**: Select `Java`.
6.  **Build Command**: `./mvnw clean package -DskipTests`
7.  **Start Command**: `java -jar target/eventhub-0.0.1-SNAPSHOT.jar`
8.  Scroll down to **Environment Variables** and add all the Render variables listed in Section 2.
9.  Click **Create Web Service**. Wait 3-5 minutes for it to build and deploy.
10. Once live, copy your backend URL (e.g., `https://eventhub-api-xyz.onrender.com`).

### Step 3: Deploy Frontend (Vercel)
1.  Sign up for **Vercel.com** and click **Add New** -> **Project**.
2.  Connect your GitHub repository.
3.  **Framework Preset**: Select `Vite`.
4.  **Root Directory**: Edit and select `frontend`.
5.  **Build Command**: `npm run build`
6.  **Output Directory**: `dist`
7.  Open **Environment Variables** and add:
    *   Name: `VITE_API_BASE_URL`
    *   Value: `https://eventhub-api-xyz.onrender.com` (Your Render URL)
8.  Click **Deploy**.
9.  Once deployed, copy your Vercel URL and add it to Render's `FRONTEND_URL` environment variable (then manually click "Manual Deploy -> Restart" on Render so it picks up the CORS change).

Your EventHub application is now live!
