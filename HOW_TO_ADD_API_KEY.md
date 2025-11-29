# 🔑 How to Add Your Gemini API Key

## Quick Start

### Step 1: Get a FREE Gemini API Key
1. Go to: https://makersuite.google.com/app/apikey
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy your new API key

### Step 2: Add to config.properties
1. Open the file `config.properties` in the project root
2. Find the line: `ai.gemini.api.key=YOUR_NEW_API_KEY_HERE`
3. Replace `YOUR_NEW_API_KEY_HERE` with your actual API key
4. Save the file

**Example:**
```properties
ai.gemini.api.key=AIzaSyABC123def456GHI789jkl012MNO345pqr
```

### Step 3: Run the App
That's it! The app will now use real AI features instead of mock responses.

---

## ✅ Security Checklist

- ✅ `config.properties` is in `.gitignore` (won't be pushed to GitHub)
- ✅ Never put API keys in source code files
- ✅ Never commit `config.properties` to git
- ✅ Keep your API key private

---

## 🎯 Where to Put the API Key

### ✅ CORRECT - Use config.properties
```
NoteSmith/
├── config.properties  ← PUT YOUR API KEY HERE
├── src/
└── ...
```

### ❌ WRONG - Don't put in source code
```java
// DON'T DO THIS!
public static String getGeminiApiKey() {
    return "AIzaSy...";  // ❌ NEVER hardcode API keys
}
```

---

## 🔄 Alternative: Environment Variables

You can also use environment variables (useful for production):

**Windows:**
```cmd
set AI_GEMINI_API_KEY=your_api_key_here
```

**Linux/Mac:**
```bash
export AI_GEMINI_API_KEY=your_api_key_here
```

The app checks environment variables first, then falls back to `config.properties`.

---

## 🧪 Testing Without API Key

Don't have an API key yet? No problem!

The app includes **Mock AI Mode** that provides demo responses:
- Mock summaries
- Mock tag suggestions  
- Mock related notes (keyword matching)

Just leave the API key empty or set to `YOUR_NEW_API_KEY_HERE` and the app will automatically use mock mode.

---

## 🆘 Troubleshooting

### "AI is disabled"
- Check that `ai.enabled=true` in config.properties

### "Mock Mode" showing instead of real AI
- Verify your API key is correct
- Make sure there are no extra spaces
- Check the key hasn't been revoked

### API errors
- Verify your API key is valid at https://makersuite.google.com/app/apikey
- Check your internet connection
- Ensure you haven't exceeded API quota

---

## 📝 Current config.properties Location

```
C:\Users\user\JavaProjects\NoteSmith\config.properties
```

**This file is gitignored and safe for your API key!** ✅
