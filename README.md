# Product-Based Capstone "Health Alert"
HealthAlert is an Android-based mobile application designed to provide a quick and convenient health monitoring solution for individuals with busy lifestyles. By utilizing smartphone-based Photoplethysmography (PPG) technology, the app helps detect potential heart disease risks early, without requiring routine doctor visits. The project integrates advanced machine learning models, an intuitive mobile app, and robust cloud services to deliver a seamless user experience. The goal of HealthAlert is to empower users to take a proactive approach to their heart health, bridging the gap between their busy schedules and the need for regular health check-ups.

![image](https://github.com/user-attachments/assets/658af91b-f47d-430c-8cc4-e2bc82c7277b)


# Backend API - Node.js with Hapi.js

This repository contains the backend services for our application, built using Node.js and the Hapi.js framework. The backend handles API endpoints, business logic, and integration with the database and cloud storage.

## Prerequisites

Before setting up the project, ensure you have the following installed:

- [Node.js](https://nodejs.org/) (v14)
- [npm](https://www.npmjs.com/) or [yarn](https://yarnpkg.com/)
- Access to a Cloud SQL instance
- Cloud Storage bucket configured

## Setup Instructions

1. Install dependencies:

   ```bash
   npm install
   # or
   yarn install
   ```

2. Configure environment variables:

   Create a `.env` file in the root directory and provide the necessary configurations:

   ```env
   PORT=3000
   DATABASE_URL=your-database-url
   CLOUD_STORAGE_BUCKET=your-bucket-name
   ```

3. Start the server:

   ```bash
   npm run start
   # or
   yarn dev
   ```

   The server will run on `http://localhost:3000` by default.

## API Documentation

The API endpoints and their functionalities are documented in the [API Documentation](https://documenter.getpostman.com/view/40190244/2sAYBbeUmB).

## Deployment

To deploy the backend, you can use platforms like Google Cloud Run. Ensure your environment variables and cloud services are properly configured for production.

## Downloadable APP

To access the app, you can download the app from the link provided. [HealthAlert APP](https://drive.google.com/file/d/1iO54bYQ0eoQZTvWS20C9Us-QVPrrU6E5/view?usp=drive_link).

