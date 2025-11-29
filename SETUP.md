# NoteSmith Setup Guide

## Prerequisites
- Java 11 or higher
- MySQL Server (XAMPP recommended for Windows)
- MySQL Connector/J library (included in `lib/`)
- jBCrypt library (included in `lib/`)

## Database Setup

1. Start MySQL server (via XAMPP or standalone)

2. Create the database and tables:

```sql
CREATE DATABASE notesmith_db;

USE notesmith_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notes (
    id VARCHAR(36) PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    done BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC)
);
```

## Configuration

1. Copy `config.properties.example` to `config.properties`
2. Update database credentials if needed:
   - Default XAMPP MySQL: user=root, password=(empty)
   - For production, use strong credentials

## Running the Application

### From IDE (IntelliJ IDEA / Eclipse)
1. Open project
2. Add libraries from `lib/` folder to classpath
3. Run `com.notesmith.Main`

### From Command Line
```bash
# Compile
javac -cp "lib/*" -d out src/com/notesmith/**/*.java

# Run
java -cp "out;lib/*" com.notesmith.Main
```

## Features

### Security
- Password hashing with BCrypt
- Password strength validation
- Username format validation
- Connection pooling for database

### Note Management
- Create, edit, delete notes
- Markdown formatting (bold, italic, lists)
- Live preview
- Dual persistence (file + database)
- Undo/Redo support

### Keyboard Shortcuts
- `Ctrl+S` - Save note
- `Ctrl+N` - New note
- `Ctrl+Z` - Undo
- `Ctrl+Y` - Redo
- `Delete` - Delete selected note (with confirmation)

## Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (!@#$%^&*(),.?":{}|<>)

## Troubleshooting

### Database Connection Failed
- Ensure MySQL server is running
- Check credentials in `config.properties`
- Verify database `notesmith_db` exists

### ClassNotFoundException
- Ensure MySQL Connector and jBCrypt JARs are in `lib/` folder
- Add libraries to project classpath

### Port Already in Use
- Default MySQL port is 3306
- Change port in `config.properties` if needed
