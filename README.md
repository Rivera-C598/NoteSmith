# NoteSmith 📝

A modern, feature-rich note-taking application built with Java Swing. NoteSmith combines elegant design with powerful functionality, offering markdown support, dual persistence, and robust security.

## ✨ Features

### Core Functionality
- **Rich Text Editing** - Markdown formatting with live preview
- **Dual Persistence** - Automatic backup to both database and file system
- **User Authentication** - Secure login with BCrypt password hashing
- **Note Management** - Create, edit, delete, and organize notes effortlessly

### Security
- ✅ BCrypt password hashing (cost factor: 12)
- ✅ Password strength validation and indicator
- ✅ Username format validation
- ✅ Connection pooling for database security
- ✅ SQL injection protection via prepared statements

### User Experience
- 🎨 Modern dark theme UI
- ⌨️ Keyboard shortcuts (Ctrl+S, Ctrl+N, Ctrl+Z, Ctrl+Y)
- 🔄 Undo/Redo support
- 📊 Live markdown preview
- ⚡ Fast and responsive interface
- ✅ Confirmation dialogs for destructive actions

### Markdown Support
- **Bold** text with `**text**`
- *Italic* text with `_text_`
- Bullet lists with `- item`
- Real-time preview pane

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- MySQL Server (XAMPP recommended)
- Libraries included in `lib/`:
  - MySQL Connector/J 9.5.0
  - jBCrypt 0.4

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd NoteSmith
   ```

2. **Setup database**
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

3. **Configure application**
   ```bash
   cp config.properties.example config.properties
   # Edit config.properties with your database credentials
   ```

4. **Run the application**
   - **From IDE**: Run `com.notesmith.Main`
   - **From CLI**: 
     ```bash
     javac -cp "lib/*" -d out src/com/notesmith/**/*.java
     java -cp "out;lib/*" com.notesmith.Main
     ```

## 📖 Usage

### Registration
1. Click "Create an account"
2. Enter username (3-20 alphanumeric characters)
3. Create strong password (min 8 chars, uppercase, lowercase, digit, special char)
4. Watch the password strength indicator
5. Click "Register"

### Creating Notes
1. Login with your credentials
2. Enter note title and content
3. Use markdown formatting buttons or shortcuts
4. Press `Ctrl+S` or click "Add Note"
5. View live preview in the right pane

### Keyboard Shortcuts
| Shortcut | Action |
|----------|--------|
| `Ctrl+S` | Save current note |
| `Ctrl+N` | Create new note |
| `Ctrl+Z` | Undo |
| `Ctrl+Y` | Redo |
| `Delete` | Delete selected note |

## 🏗️ Architecture

```
src/com/notesmith/
├── config/          # Configuration and styling
│   ├── AppConfig.java
│   ├── AppStyles.java
│   ├── DbConfig.java
│   └── UIConstants.java
├── exception/       # Custom exception hierarchy
│   ├── NoteSmithException.java
│   ├── AuthenticationException.java
│   ├── ValidationException.java
│   └── PersistenceException.java
├── model/           # Domain models
│   ├── Note.java
│   ├── TextNote.java
│   ├── TodoNote.java
│   ├── NoteType.java
│   └── User.java
├── persistence/     # Data access layer
│   ├── Database.java
│   ├── ConnectionPool.java
│   ├── NoteRepository.java
│   ├── UserRepository.java
│   ├── JdbcNoteRepository.java
│   ├── JdbcUserRepository.java
│   ├── FileNoteRepository.java
│   └── DualNoteRepository.java
├── security/        # Security utilities
│   └── PasswordHasher.java
├── ui/              # User interface
│   ├── NoteSmithApp.java
│   ├── LoginPanel.java
│   ├── RegisterPanel.java
│   ├── DashboardPanel.java
│   └── components/  # Reusable UI components
├── util/            # Utility classes
│   ├── Logger.java
│   └── ValidationUtils.java
└── Main.java        # Application entry point
```

## 🔧 Configuration

Edit `config.properties` to customize:

```properties
# Database
db.url=jdbc:mysql://localhost:3306/notesmith_db
db.user=root
db.password=your_password
db.pool.size=10

# Security
security.max.login.attempts=5
security.password.min.length=8
```

Or use environment variables:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `DB_POOL_SIZE`
- `SECURITY_MAX_LOGIN_ATTEMPTS`
- `SECURITY_PASSWORD_MIN_LENGTH`

## 🛠️ Development

### Project Structure
- **Model Layer**: Domain entities (Note, User)
- **Persistence Layer**: Repository pattern with dual storage
- **UI Layer**: Swing components with MVC pattern
- **Security Layer**: Password hashing and validation
- **Configuration Layer**: Centralized settings management

### Design Patterns Used
- Repository Pattern (data access)
- Strategy Pattern (dual persistence)
- Observer Pattern (UI updates)
- Singleton Pattern (connection pool)
- Factory Pattern (note creation)

### Code Quality
- ✅ Proper exception handling
- ✅ Connection pooling
- ✅ Prepared statements (SQL injection prevention)
- ✅ Input validation
- ✅ Logging framework
- ✅ Constants for magic numbers
- ✅ Clean separation of concerns

## 🐛 Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Check credentials in `config.properties`
- Verify database exists: `SHOW DATABASES;`

### ClassNotFoundException
- Ensure JARs are in `lib/` folder
- Add to classpath when compiling/running

### UI Not Displaying Correctly
- Check Java version (requires 11+)
- Try different Look and Feel if Nimbus unavailable

## 📝 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Support

For issues and questions, please open an issue on GitHub.

---

**Built with ❤️ using Java Swing**
