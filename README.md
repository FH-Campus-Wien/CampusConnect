# 🎓 CampusConnect

<p align="center">
  <img src="src/main/resources/at/ac/hcw/campusconnect/images/logo.png" alt="CampusConnect Logo" width="200"/>
</p>

<p align="center">
  <strong>A Social Discovery Platform for University Students</strong>
</p>

<p align="center">
  Connect with fellow students, find study partners, make friends, and discover your campus community.
</p>

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/a6cf867f-8be6-40e4-9eff-30b05da18dc7" alt="Login Screen" width="250"/>
  <img src="https://github.com/user-attachments/assets/2fabe6d4-9968-4d98-b7b5-5c7c8951ac13" alt="Profile Setup Screen 1" width="250">
  <img src="https://github.com/user-attachments/assets/7a1fb289-8955-48a2-b24e-5db772c63fe7" alt="Profile Setup Screen 2" width="250">
  <img src="https://github.com/user-attachments/assets/efc526e3-6bd4-41f2-9998-b1a1f7ec3d75" alt="Discover Screen" width="250"/>
  <img src="https://github.com/user-attachments/assets/14156e5a-f211-4f4e-8790-087da6d092a1" alt="Matches Screen" width="250"/>
    <img src="https://github.com/user-attachments/assets/ec8b7804-a7a4-4760-a473-c1734dd618f1" alt="Chats Screen" width="250"/>
</p>

---

## ✨ Features

### 🔐 Authentication

- **Email-based OTP Login** - Secure, passwordless authentication using one-time passwords
- **Session Management** - Persistent login with automatic token refresh
- **Profile Setup** - Comprehensive onboarding for new users

### 👥 Social Discovery

- **Smart Matching** - Swipe through profiles based on your preferences
- **Customizable Filters** - Filter by interests, degree type, semester, and more
- **Interest Tags** - Express yourself with customizable interests and hobbies
- **Profile Preferences** - Specify what you're looking for: friends, study partners, or dates

### 💬 Communication

- **Real-time Chat** - Message your matches instantly
- **Match Notifications** - Get notified when you have a new match
- **Chat History** - Keep track of all your conversations

### 🎨 User Experience

- **Modern UI** - Clean, intuitive JavaFX interface
- **Responsive Design** - Optimized for different screen sizes
- **Profile Customization** - Upload photos, write bios, and showcase your personality
- **Settings Management** - Control your preferences and privacy settings

---

## 🛠️ Tech Stack

### Frontend

- **JavaFX 21** - Modern desktop application framework
- **FXML** - Declarative UI design
- **CSS** - Custom styling and theming

### Backend & Services

- **Supabase** - Backend-as-a-Service for authentication and database
- **PostgreSQL** - Relational database for user profiles and matches
- **Java HTTP Client** - Native HTTP/2 client for API communication

### Development

- **Java 21** - Latest LTS version with modern language features
- **Gradle** - Build automation and dependency management
- **Lombok** - Boilerplate code reduction
- **Jackson** - JSON serialization/deserialization

---

## 📋 Prerequisites

- **Java Development Kit (JDK) 21** or higher
- **Gradle 8.x** (included via wrapper)
- **Supabase Account** - For backend services

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/FH-Campus-Wien/CampusConnect.git
cd CampusConnect
```

### 2. Configure Environment Variables

Create a `.env` file in the project root:

```env
SUPABASE_URL=your_supabase_project_url
SUPABASE_ANON_KEY=your_supabase_anon_key
```

> 💡 **Tip:** You can find these values in your Supabase project settings under API.

### 3. Set Up the Database

Use the Supabase SQL Editor to run the scripts directly.

Or use the Supabase CLI

```bash
# Connect to your Supabase database and run:
psql -h your-db-host -U postgres -d postgres -f sql/schema.sql
```

---

## 📁 Project Structure

```
CampusConnect/
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java
│       │   └── at/ac/hcw/campusconnect/
│       │       ├── CampusConnectApplication.java
│       │       ├── Launcher.java
│       │       ├── components/          # Reusable UI components
│       │       ├── config/              # Configuration classes
│       │       ├── controller/          # FXML controllers
│       │       ├── models/              # Data models
│       │       ├── services/            # Business logic & API services
│       │       └── util/                # Utility classes
│       └── resources/
│           └── at/ac/hcw/campusconnect/
│               ├── *.fxml               # UI layouts
│               ├── images/              # Image assets
│               └── styles/              # CSS stylesheets
├── sql/
│   ├── schema.sql                       # Database schema
│   └── mockdata.sql                     # Sample data
├── build.gradle                         # Build configuration
├── settings.gradle
└── .env                                 # Environment variables (not in git)
```

---

## 🗄️ Database Schema

### Main Tables

- **`profiles`** - User profile information (name, bio, interests, photos)
- **`user_actions`** - User interactions (likes, passes)
- **`matches`** - Mutual matches between users
- **`messages`** - Chat messages between matched users

### Key Features

- **Row Level Security (RLS)** - Ensures users can only access their own data
- **Triggers** - Automatic match creation when mutual likes occur
- **Indexes** - Optimized queries for fast matching and discovery

---

## 🎮 Usage

### First Time Setup

1. **Launch the app** - The login screen will appear
2. **Enter your university email** - Use your @fh-campuswien.ac.at address
3. **Enter the OTP** - Check your email for the verification code
4. **Complete your profile** - Add your details, interests, and photos
5. **Start discovering** - Swipe right to like, left to pass

### Finding Matches

- Navigate to the **Discover** tab
- Browse through student profiles
- Swipe or click based on your interest
- Get notified when there's a mutual match!

### Chatting

- Go to the **Matches** tab to see your connections
- Click on a match to start chatting
- Messages are delivered in real-time

---

## 📝 License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

---

## 👥 Team

Developed by students at **FH Campus Wien** as part of the 1st semester Programming course.

---
