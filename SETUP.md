# NoteSmith Setup Guide

Complete step-by-step guide to set up and run NoteSmith.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Database Setup](#database-setup)
3. [Application Configuration](#application-configuration)
4. [Running the Application](#running-the-application)
5. [AI Configuration (Optional)](#ai-configuration-optional)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 11 or higher**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`

2. **MySQL Server**
   - **Option A: XAMPP** (Recommended for Windows)
     - Download from: https://www.apachefriends.org/
     - Includes MySQL, phpMyAdmin, and Apache
   - **Option B: Standalone MySQL**
     - Download from: https://dev.mysql.com/downloads/mysql/

3. **IDE (Recommended)**
   - IntelliJ IDEA (Community or Ultimate)
   - Eclipse
   - Or any Java IDE

### Included Libraries
The following libraries are already included in the `lib/` folder:
- `mysql-connector-j-9.5.0.jar` - MySQL JDBC driver
- `jbcrypt-0.4.jar` - Password hashing library

---

## Database Setup

### Step 1: Start MySQL Server

**Using XAMPP:**
1. Open XAMPP Control Panel
2. Click "Start" next to MySQL
3. Wait for it to show "Running" status

**Using Standalone MySQL:**
1. Start MySQL service from Services (Windows)
2. Or run: `mysql.server start` (Mac/Linux)

### Step 2: Create Database

**Option A: Using phpMyAdmin (XAMPP)**
1. Open browser and go to: `http://localhost/phpmyadmin`
2. Click "New" in the left sidebar
3. Database name: `notesmith_db`
4. Collation: `utf8mb4_general_ci`
5. Click "Create"

**Option B: Using MySQL Command Line**
```bash
mysql -u root -p
```

Then run:
```sql
CREATE DATABASE notesmith_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE notesmith_db;
```

### Step 3: Create Tables

Run the following SQL commands:

```sql
-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Notes table
CREATE TABLE notes (
    id VARCHAR(36) PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    done BOOLEAN DEFAULT FALSE,
    tags VARCHAR(500) DEFAULT '',
    pinned BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_pinned_created (user_id, pinned DESC, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 4: Verify Tables

```sql
SHOW TABLES;
-- Should show: notes, users

DESCRIBE users;
DESCRIBE notes;
```

---

## Application Configuration

### Step 1: Create Configuration File

1. Navigate to project root directory
2. Copy the example config:
   ```bash
   cp config.properties.example config.properties
   ```

### Step 2: Edit Configuration

Open `config.properties` and update:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/notesmith_db?useSSL=false&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_PASSWORD_HERE
db.pool.size=10

# Security Settings
security.max.login.attempts=5
security.password.min.length=3

# AI Configuration (Optional - see AI section below)
ai.enabled=true
ai.gemini.api.key=
ai.gemini.model=gemini-2.5-flash-latest
ai.cache.enabled=true
ai.max.tokens=1000
```

**Important Notes:**
- Default XAMPP MySQL password is empty (leave blank)
- `config.properties` is gitignored - safe for passwords
- Don't commit this file to version control

---

## Running the Application

### Option 1: Using IntelliJ IDEA (Recommended)

1. **Open Project**
   - File → Open → Select NoteSmith folder
   - Wait for indexing to complete

2. **Add Libraries to Classpath**
   - File → Project Structure → Libraries
   - Click "+" → Java
   - Select both JAR files in `lib/` folder
   - Click OK

3. **Configure Run Configuration**
   - Run → Edit Configurations
   - Click "+" → Application
   - Name: "NoteSmith"
   - Main class: `com.notesmith.Main`
   - Click OK

4. **Copy config.properties to Output**
   ```bash
   cp config.properties out/production/NoteSmith/
   ```
   
   **Note**: You'll need to do this after each rebuild, or:
   - Create `src/resources/` folder
   - Right-click → Mark Directory as → Resources Root
   - Move `config.properties` there

5. **Run the Application**
   - Click the green Run button (▶️)
   - Or press `Shift + F10`

### Option 2: Using Command Line

1. **Compile**
   ```bash
   javac -cp "lib/*" -d out src/com/notesmith/**/*.java
   ```

2. **Copy config.properties**
   ```bash
   cp config.properties out/
   ```

3. **Run**
   ```bash
   # Windows
   java -cp "out;lib/*" com.notesmith.Main

   # Mac/Linux
   java -cp "out:lib/*" com.notesmith.Main
   ```

### Option 3: Using Eclipse

1. **Import Project**
   - File → Import → Existing Projects into Workspace
   - Select NoteSmith folder

2. **Add Libraries**
   - Right-click project → Build Path → Configure Build Path
   - Libraries tab → Add External JARs
   - Select both JARs from `lib/` folder

3. **Run**
   - Right-click `Main.java` → Run As → Java Application

---

## AI Configuration (Optional)

NoteSmith includes AI-powered features using Google's Gemini API. **The app works perfectly without AI** using Mock Mode.

### Step 1: Get API Key (Free)

1. Go to: https://makersuite.google.com/app/apikey
2. Sign in with Google account
3. Click "Create API Key"
4. Copy the key

### Step 2: Add to Configuration

Edit `config.properties`:
```properties
ai.enabled=true
ai.gemini.api.key=YOUR_API_KEY_HERE
```

### Step 3: Verify

Run the app and check the AI Insights panel:
- **"● API Connected"** - Real AI features enabled
- **"● Mock Mode"** - Using demo responses (no API key)

### AI Features

With API key:
- Smart note linking
- AI-powered summarization
- Intelligent tag suggestions

Without API key (Mock Mode):
- Generic summaries
- Keyword-based linking
- Random tag suggestions

---

## Troubleshooting

### Database Connection Failed

**Problem**: `SQLException: Access denied for user 'root'@'localhost'`

**Solutions**:
1. Check MySQL is running (XAMPP Control Panel)
2. Verify password in `config.properties`
3. Try empty password for XAMPP: `db.password=`
4. Reset MySQL root password if needed

**Problem**: `Unknown database 'notesmith_db'`

**Solution**: Run the CREATE DATABASE command again

### ClassNotFoundException

**Problem**: `ClassNotFoundException: com.mysql.cj.jdbc.Driver`

**Solutions**:
1. Verify `mysql-connector-j-9.5.0.jar` is in `lib/` folder
2. Add library to project classpath in IDE
3. Check compile command includes `-cp "lib/*"`

### Config File Not Found

**Problem**: App shows "Mock Mode" even with API key configured

**Solutions**:
1. Copy `config.properties` to `out/production/NoteSmith/`
2. Or create `src/resources/` and mark as Resources Root
3. Rebuild project after moving config file

### Port Already in Use

**Problem**: MySQL won't start - port 3306 in use

**Solutions**:
1. Stop other MySQL instances
2. Change port in `config.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3307/notesmith_db...
   ```
3. Update MySQL to use new port

### Emoji Boxes Showing

**Problem**: Buttons show `[]` instead of icons

**Solution**: This is normal on some Windows systems. The app works perfectly - it's just missing emoji font support. All emojis have been removed in the latest version.

---

## First Time Usage

### 1. Register an Account
- Click "Create an account"
- Username: 3-20 alphanumeric characters
- Password: Minimum 3 characters
- Click "Register"

### 2. Login
- Enter your username and password
- Click "Login"

### 3. Create Your First Note
- Click "New Note" or press `Ctrl+N`
- Enter a title
- Write your content (supports markdown)
- Add tags (optional)
- Check "Pin this note" to keep it at top (optional)
- Click "Add Note" or press `Ctrl+S`

### 4. Try AI Features (if configured)
- Select a note
- Click "Summarize" for AI summary
- Click "Find Related" to discover connections
- Click "Suggest Tags" for AI tag recommendations

---

## Advanced Configuration

### Environment Variables

Instead of `config.properties`, you can use environment variables:

**Windows:**
```cmd
set DB_URL=jdbc:mysql://localhost:3306/notesmith_db
set DB_USER=root
set DB_PASSWORD=your_password
set AI_GEMINI_API_KEY=your_api_key
```

**Linux/Mac:**
```bash
export DB_URL=jdbc:mysql://localhost:3306/notesmith_db
export DB_USER=root
export DB_PASSWORD=your_password
export AI_GEMINI_API_KEY=your_api_key
```

### Connection Pool Settings

Adjust for better performance:
```properties
db.pool.size=20  # Increase for more concurrent connections
```

### Security Settings

```properties
security.max.login.attempts=5  # Lock after failed attempts
security.password.min.length=8  # Enforce stronger passwords
```

---

## Database Maintenance

### Backup Database

```bash
mysqldump -u root -p notesmith_db > backup.sql
```

### Restore Database

```bash
mysql -u root -p notesmith_db < backup.sql
```

### View All Users

```sql
SELECT id, username, created_at FROM users;
```

### View All Notes

```sql
SELECT id, title, user_id, pinned, tags FROM notes;
```

---

## Support

If you encounter issues:
1. Check this guide's Troubleshooting section
2. Verify all prerequisites are installed
3. Check MySQL and Java versions
4. Review console output for error messages
5. Open an issue on GitHub

---

**Setup complete! Enjoy using NoteSmith! 🚀**
