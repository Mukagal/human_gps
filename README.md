# Maman-Tap 🤝

> A community-driven social platform for connecting people, sharing posts, and requesting help — built with FastAPI and Kotlin.

---

## Problem Statement

In many communities, people struggle to find help nearby or connect with others around them. Maman-Tap solves this by providing a mobile-first platform where users can post updates, chat with each other, request or offer help, and discover people near their location — all in one place.

---

## Features

### Backend (FastAPI)
- **Authentication** — JWT-based login/signup with access & refresh tokens, token blocklist via Redis, email verification, password reset
- **Role-Based Access Control** — user/admin roles with `RoleChecker` dependency
- **Posts** — create, read, update, delete posts with image upload (Cloudinary), likes, comments, shares
- **Direct Messaging** — one-on-one conversations with message history
- **Komek (Help Requests)** — post help requests by category (tutor, physical, rental, other), apply to help others, accept/reject applications
- **Location Services** — update user location, discover nearby users within a radius using Haversine formula
- **AI Image Moderation** — automatic content moderation via Sightengine API; flags and bans users who post inappropriate content
- **Email Notifications** — email confirmation and password reset via Gmail SMTP
- **Rate Limiting** — per-IP request throttling with slowapi (write endpoints limited separately from reads)

### Android (Kotlin)
- **User Profile** — view and edit profile, upload profile image
- **Posts Feed** — browse, create, and interact with posts
- **Direct Chat** — real-time one-on-one messaging
- **Komek** — browse and post help requests in the community
- **Nearby Map** — discover users near your location using Google Maps SDK

---

## Technology Stack

| Layer | Technology |
|---|---|
| **Mobile** | Kotlin (Android) |
| **Backend** | FastAPI (Python) |
| **Database** | PostgreSQL via Neon DB (cloud) |
| **ORM** | SQLModel + SQLAlchemy (async) |
| **Background Tasks** | Celery |
| **Message Broker** | Redis (Redis Labs cloud / local) |
| **Image Storage** | Cloudinary |
| **Image Moderation** | Sightengine API |
| **Email** | Gmail SMTP via fastapi-mail |
| **Authentication** | JWT (PyJWT) + bcrypt |
| **Rate Limiting** | slowapi |
| **Profiling** | pyinstrument |
| **Maps (Android)** | Google Cloud Console Maps SDK |
| **DB Migrations** | Alembic |
| **Deployment** | Render (API + Celery worker) |

---

## Demo


## Installation

### Prerequisites
- Python 3.10+
- Android Studio (for mobile)


### Environment Variables

```env
# Database
DATABASE_URL=postgresql+asyncpg://...

# Redis
REDIS_URL=redis://...

# JWT
JWT_SECRET=your_secret_key
JWT_ALGORITHM=HS256
REFRESH_TOKEN_EXPIRY=7

# Cloudinary
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

# Email (Gmail App Password)
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx
MAIL_FROM=your@gmail.com
MAIL_PORT=587
MAIL_SERVER=smtp.gmail.com
MAIL_FROM_NAME=Maman-Tap

# Sightengine (Image Moderation)
SIGHTENGINE_API_USER=
SIGHTENGINE_API_SECRET=
```

---

## Usage

### API Endpoints Overview

#### Auth
```
POST   /api/v1/signup                     Register (no email verification)
POST   /api/v1/signup-with-verification   Register with email confirmation
POST   /api/v1/login                      Login → returns access + refresh tokens
POST   /api/v1/logout                     Invalidate token
POST   /api/v1/refresh                    Refresh access token
GET    /api/v1/verify-email?token=...     Verify email
POST   /api/v1/forgot-password            Request password reset
POST   /api/v1/reset-password             Reset password with token
```

#### Users
```
GET    /api/v1/users                      List users (search by username)
GET    /api/v1/users/me                   Get current user
PATCH  /api/v1/users/me                   Update profile
DELETE /api/v1/users/me                   Delete account
POST   /api/v1/users/me/profile-image     Upload profile image
PATCH  /api/v1/users/me/location          Update location
GET    /api/v1/users/nearby               Find nearby users
```

#### Posts
```
GET    /api/v1/posts                      Feed (sort by latest/likes/comments)
POST   /api/v1/posts                      Create post (with optional image)
PATCH  /api/v1/posts/{id}                 Update post
DELETE /api/v1/posts/{id}                 Delete post
POST   /api/v1/posts/{id}/like            Like post
DELETE /api/v1/posts/{id}/like            Unlike post
POST   /api/v1/posts/{id}/comments        Add comment
POST   /api/v1/posts/{id}/share           Share post to conversation/group
```

#### Conversations & Messages
```
POST   /api/v1/conversations              Start conversation
GET    /api/v1/users/{id}/conversations   List conversations
POST   /api/v1/conversations/{id}/messages  Send message
GET    /api/v1/conversations/{id}/messages  Get messages
PATCH  /api/v1/messages/{id}              Edit message
DELETE /api/v1/messages/{id}              Delete message
```

#### Komek (Help Requests)
```
POST   /api/v1/komek/requests             Create help request
GET    /api/v1/komek/requests             Browse requests (filter by category)
POST   /api/v1/komek/requests/{id}/apply  Apply to help
PATCH  /api/v1/komek/applications/{id}    Accept or reject application
```

#### Groups
```
POST   /api/v1/groups                     Create group
GET    /api/v1/groups/{id}                Get group info
POST   /api/v1/groups/{id}/members        Add member
DELETE /api/v1/groups/{id}/members/{uid}  Remove member
POST   /api/v1/groups/{id}/messages       Send group message
```

---

## Background Tasks

| Task | Trigger | Description |
|---|---|---|
| `send_confirmation_email` | Signup with verification | Sends email verification link |
| `send_password_reset_email` | Forgot password | Sends password reset link |
| `compress_and_store_image` | Profile image upload | Compresses image, stores binary in PostgreSQL |
| `moderate_image` | Post with image | AI content check; flags post + bans user if inappropriate |
| `moderate_profile_image` | Profile image upload | AI content check; removes image + bans user if inappropriate |

---

## Architecture

```
Android App (Kotlin)
       │
       ▼ HTTP/REST
FastAPI Backend ──── PostgreSQL (Neon DB)
       │
       ├──── Redis (rate limiting + JWT blocklist + Celery broker)
       │
       └──── Celery Worker
                ├── Email tasks (Gmail SMTP)
                └── Image moderation (Sightengine API → ban user)
```

---

## Deployment

The backend is deployed on **Render** using two services from the same GitHub repository:

- **Web Service** — FastAPI app
  - Build: `pip install -r requirements.txt`
  - Start: `uvicorn src:app --host 0.0.0.0 --port $PORT`

- **Web Service (Celery)** — Background worker
  - Build: `pip install -r requirements.txt`
  - Start: `python -m celery -A src.celery worker --loglevel=INFO --pool=solo`

External services: **Neon DB** (PostgreSQL) + **Redis Labs** (Redis)
