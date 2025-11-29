# NoteSmith

> A modern, AI-powered note-taking application built with Java Swing

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## Overview

NoteSmith is a feature-rich desktop note-taking application that combines elegant design with powerful functionality. It offers markdown support, dual persistence (database + file backup), secure authentication, and AI-powered features using Google's Gemini API.

## Features

### Core Features
- **Rich Text Editing** - Markdown formatting with live preview
- **Dual Persistence** - Automatic backup to both MySQL database and file system
- **User Authentication** - Secure login with BCrypt password hashing
- **Search & Filter** - Real-time search across all notes
- **Tags System** - Organize notes with multiple tags
- **Pin Notes** - Keep important notes at the top
- **Export** - Export notes to Markdown or HTML format

### AI-Powered Features (Optional)
- **Smart Linking** - AI discovers connections between notes
- **Auto-Summarization** - Generate TL;DR for long notes
- **Tag Suggestions** - AI suggests relevant tags based on content
- **Mock Mode** - Works without API key using demo responses

### Security
- BCrypt password hashing (cost factor: 12)
- Password strength validation
- SQL injection protection via prepared statements
- Connection pooling for database security
- Secure API key management

### User Experience
- Modern dark theme UI
- Keyboard shortcuts (Ctrl+S, Ctrl+N, Ctrl+Z, Ctrl+Y, Delete)
- Undo/Redo support
- Live markdown preview
- Confirmation dialogs for destructive actions
- Responsive interface

## Quick Start

### Prerequisites
- **Java 11 or higher**
- **MySQL Server** (XAMPP recommended for Windows)
- **Libraries** (included in `lib/` folder):
  - MySQL Connector/J 9.5.0
  - jBCrypt 0.4

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Rivera-C598/NoteSmith.git
   cd NoteSmith
   ```

2. **Setup database** (see [SETUP.md](SETUP.md) for detailed instructions)
   ```sql
   CREATE DATABASE notesmith_db;
   -- Run the SQL scripts from SETUP.md
   ```

3. **Configure the application**
   ```bash
   cp config.properties.example config.properties
   # Edit config.properties with your settings
   ```

4. **Run the application**
   - Open project in IntelliJ IDEA
   - Run `com.notesmith.Main`

## Configuration

### Database Configuration
Edit `config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/notesmith_db
db.user=root
db.password=your_password
```

### AI Configuration (Optional)
Get a free API key from [Google AI Studio](https://makersuite.google.com/app/apikey):
```properties
ai.enabled=true
ai.gemini.api.key=YOUR_API_KEY_HERE
```

**Note**: The app works perfectly without an API key using Mock AI mode!

## Usage

### Keyboard Shortcuts
| Shortcut | Action |
|----------|--------|
| `Ctrl+S` | Save current note |
| `Ctrl+N` | Create new note |
| `Ctrl+E` | Export selected note |
| `Ctrl+Z` | Undo |
| `Ctrl+Y` | Redo |
| `Delete` | Delete selected note |

### Markdown Formatting
- **Bold**: `**text**`
- *Italic*: `_text_`
- Lists: `- item`

## Architecture

```
src/com/notesmith/
├── ai/              # AI integration (Gemini API)
├── config/          # Configuration and styling
├── exception/       # Custom exception hierarchy
├── model/           # Domain models (Note, User)
├── persistence/     # Data access layer
├── security/        # Password hashing
├── ui/              # Swing UI components
└── util/            # Utility classes
```

### Design Patterns
- **Repository Pattern** - Data access abstraction
- **Strategy Pattern** - Dual persistence implementation
- **Observer Pattern** - UI updates
- **Singleton Pattern** - Connection pool
- **Factory Pattern** - Note creation

## Development

### Project Structure
- `src/` - Source code
- `lib/` - External libraries
- `out/` - Compiled classes
- `config.properties` - Local configuration (gitignored)

### Building
```bash
# Compile
javac -cp "lib/*" -d out src/com/notesmith/**/*.java

# Run
java -cp "out;lib/*" com.notesmith.Main
```

## Security

### API Key Safety
- ✅ Never commit `config.properties` (gitignored)
- ✅ Use environment variables for production
- ✅ API keys are never hardcoded in source

### Password Security
- Passwords hashed with BCrypt (cost: 12)
- Minimum 3 characters (configurable)
- Stored securely in database

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Check credentials in `config.properties`
- Verify database exists

### AI Features Not Working
- Check if API key is configured
- Verify internet connection
- App works in Mock Mode without API key

### ClassNotFoundException
- Ensure JARs are in `lib/` folder
- Add libraries to project classpath in IDE

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the MIT License.

## Acknowledgments

- Built with Java Swing
- Powered by Google Gemini AI
- Uses BCrypt for password hashing
- MySQL for data persistence

---

**Made with ❤️ by Rivera-C598**
