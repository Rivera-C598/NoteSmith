# 🔒 SECURITY NOTICE

## API Key Exposure - RESOLVED

**Date**: 2024-11-29

### What Happened
A Gemini API key was accidentally hardcoded in `src/com/notesmith/config/AppConfig.java` and committed to the repository.

### Actions Taken
1. ✅ API key removed from code (commit 703e8f6)
2. ⚠️ **IMPORTANT**: The exposed API key has been revoked/regenerated
3. ✅ Updated code to only use config.properties or environment variables

### For Users
If you cloned this repository before the fix:
1. **DO NOT use the old API key** - it has been revoked
2. Get your own free API key from: https://makersuite.google.com/app/apikey
3. Add it to your `config.properties` file (NOT in the code)

### Best Practices Going Forward
- ✅ Never hardcode API keys in source code
- ✅ Always use `config.properties` (which is gitignored)
- ✅ Use environment variables for production
- ✅ Keep sensitive data out of version control

### How to Configure API Key Properly

**Option 1: Using config.properties (Recommended for local dev)**
```properties
# Create config.properties in project root
ai.gemini.api.key=YOUR_NEW_API_KEY_HERE
```

**Option 2: Using Environment Variables (Recommended for production)**
```bash
# Windows
set AI_GEMINI_API_KEY=YOUR_NEW_API_KEY_HERE

# Linux/Mac
export AI_GEMINI_API_KEY=YOUR_NEW_API_KEY_HERE
```

### Verification
The application now:
- ✅ Defaults to empty string for API key
- ✅ Uses mock AI responses when no key is configured
- ✅ Reads from config.properties first
- ✅ Falls back to environment variables
- ✅ Never exposes keys in code

---

**If you have any security concerns, please create an issue or contact the maintainer.**
