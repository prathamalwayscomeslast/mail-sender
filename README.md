# 📧 MailSender

A minimal Spring Boot app for sending emails via **Microsoft 365 using OAuth2** — no passwords, no Basic Auth. Built as a research prototype for integrating SMTP XOAUTH2 with Azure AD Client Credentials flow.

## How It Works

1. Enter your Azure App Registration credentials (stored in session only)
2. Compose your email with To, CC, BCC, Reply-To support
3. App fetches an OAuth2 token from Azure AD and sends via `smtp.office365.com:587` using XOAUTH2

## Stack

- Java 21 + Spring Boot
- MSAL4J (Microsoft Authentication SDK)
- Jakarta Mail — SMTP XOAUTH2
- Thymeleaf — server-side templates

## Running Locally

```bash
git clone https://github.com/prathamalwayscomeslast/mail-sender.git
cd mail-sender
./mvnw spring-boot:run
