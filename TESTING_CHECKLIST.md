# NoteSmith Testing Checklist

## Pre-Testing Setup
- [ ] MySQL server is running
- [ ] Database `notesmith_db` created with tables
- [ ] `config.properties` file created from example
- [ ] Libraries in `lib/` folder are accessible

## 1. Registration Tests

### Valid Registration
- [ ] Register with username: `testuser1`
- [ ] Password: `Test@123456` (should show Strong)
- [ ] Verify password strength indicator changes color
- [ ] Confirm registration success message appears
- [ ] Check database: `SELECT * FROM users;`

### Invalid Registration - Username
- [ ] Try username with spaces: `test user` (should fail)
- [ ] Try username too short: `ab` (should fail)
- [ ] Try username too long: `verylongusernamethatexceedslimit` (should fail)
- [ ] Try special characters: `test@user` (should fail)
- [ ] Try duplicate username (should fail)

### Invalid Registration - Password
- [ ] Try password too short: `Test@12` (should fail)
- [ ] Try password without uppercase: `test@123456` (should fail)
- [ ] Try password without lowercase: `TEST@123456` (should fail)
- [ ] Try password without digit: `Test@abcdef` (should fail)
- [ ] Try password without special char: `Test123456` (should fail)
- [ ] Try mismatched passwords (should fail)

### Password Strength Indicator
- [ ] Type `weak` - should show red/weak
- [ ] Type `Medium1!` - should show orange/medium
- [ ] Type `Strong@Pass123` - should show green/strong

## 2. Login Tests

### Valid Login
- [ ] Login with registered credentials
- [ ] Verify welcome message shows username
- [ ] Verify dashboard loads

### Invalid Login
- [ ] Try non-existent username (should show "User not found")
- [ ] Try wrong password (should show "Invalid password")
- [ ] Try empty fields (should show validation message)

## 3. Note Creation Tests

### Valid Note Creation
- [ ] Click "New Note" or press `Ctrl+N`
- [ ] Enter title: "Test Note 1"
- [ ] Enter content: "This is a **bold** test"
- [ ] Verify preview shows bold text
- [ ] Press `Ctrl+S` or click "Add Note"
- [ ] Verify note appears in list
- [ ] Check database: `SELECT * FROM notes;`
- [ ] Check file: `notes_user_1.txt`

### Invalid Note Creation
- [ ] Try to save note with empty title (should fail)
- [ ] Try to save note with title > 200 chars (should fail)

### Markdown Formatting
- [ ] Test bold: `**bold text**` - verify preview
- [ ] Test italic: `_italic text_` - verify preview
- [ ] Test bullet list:
  ```
  - Item 1
  - Item 2
  - Item 3
  ```
- [ ] Click "B" button with selected text
- [ ] Click "I" button with selected text
- [ ] Click "•" button to add bullet

## 4. Note Editing Tests

- [ ] Click on a note in the list
- [ ] Verify title and content load in editor
- [ ] Verify button changes to "Save Changes"
- [ ] Modify title and content
- [ ] Press `Ctrl+S` to save
- [ ] Verify changes persist in list
- [ ] Verify changes in database
- [ ] Verify changes in file

## 5. Note Deletion Tests

- [ ] Select a note
- [ ] Press `Delete` key
- [ ] Verify confirmation dialog appears
- [ ] Click "No" - note should remain
- [ ] Press `Delete` again
- [ ] Click "Yes" - note should be deleted
- [ ] Verify note removed from list
- [ ] Verify note removed from database
- [ ] Verify note removed from file

### Delete Button
- [ ] Click "Delete Selected" button
- [ ] Verify confirmation dialog
- [ ] Confirm deletion

## 6. Keyboard Shortcuts Tests

- [ ] `Ctrl+N` - Creates new note (clears editor)
- [ ] `Ctrl+S` - Saves current note
- [ ] `Ctrl+Z` - Undo in title field
- [ ] `Ctrl+Z` - Undo in content area
- [ ] `Ctrl+Y` - Redo in title field
- [ ] `Ctrl+Y` - Redo in content area
- [ ] `Delete` - Delete selected note (with confirmation)

## 7. UI/UX Tests

### Color Coding
- [ ] Success messages appear in green
- [ ] Error messages appear in red
- [ ] Password strength: weak=red, medium=orange, strong=green

### Responsiveness
- [ ] Resize window - verify layout adjusts
- [ ] Drag split pane divider - verify it moves
- [ ] Scroll long note list
- [ ] Scroll long note content

### Navigation
- [ ] Click "Create an account" from login
- [ ] Click "Back to Login" from register
- [ ] Click "Logout" from dashboard
- [ ] Verify returns to login screen

## 8. Persistence Tests

### Dual Persistence
- [ ] Create a note
- [ ] Verify it exists in database: `SELECT * FROM notes;`
- [ ] Verify it exists in file: `notes_user_X.txt`
- [ ] Stop MySQL server
- [ ] Restart app
- [ ] Verify notes still load from file
- [ ] Start MySQL server
- [ ] Create new note
- [ ] Verify saves to both locations

### Data Consistency
- [ ] Create 5 notes
- [ ] Logout and login again
- [ ] Verify all 5 notes appear
- [ ] Verify order (newest first)

## 9. Error Handling Tests

### Database Errors
- [ ] Stop MySQL server
- [ ] Try to login (should show error)
- [ ] Try to register (should show error)
- [ ] Start MySQL server
- [ ] Verify app recovers

### File System Errors
- [ ] Make notes file read-only
- [ ] Try to save note
- [ ] Verify error message
- [ ] Remove read-only flag

## 10. Security Tests

### Password Hashing
- [ ] Register a user
- [ ] Check database: `SELECT password_hash FROM users;`
- [ ] Verify password is hashed (starts with `$2a$`)
- [ ] Verify hash is different from plaintext

### SQL Injection Prevention
- [ ] Try username: `admin' OR '1'='1`
- [ ] Try password: `' OR '1'='1`
- [ ] Verify login fails (not vulnerable)

### Connection Pooling
- [ ] Create 20 notes rapidly
- [ ] Verify no connection errors
- [ ] Check logs for connection pool messages

## 11. Multi-User Tests

- [ ] Register user1
- [ ] Create 3 notes for user1
- [ ] Logout
- [ ] Register user2
- [ ] Create 2 notes for user2
- [ ] Verify user2 only sees their 2 notes
- [ ] Logout and login as user1
- [ ] Verify user1 sees their 3 notes
- [ ] Check database: notes should have correct user_id
- [ ] Check files: `notes_user_1.txt` and `notes_user_2.txt` should exist

## 12. Performance Tests

- [ ] Create 50 notes
- [ ] Verify list loads quickly
- [ ] Verify scrolling is smooth
- [ ] Verify search/filter (when implemented)

## 13. Edge Cases

### Empty States
- [ ] New user with no notes - verify empty list
- [ ] Delete all notes - verify empty list

### Long Content
- [ ] Create note with 10,000 character content
- [ ] Verify saves successfully
- [ ] Verify loads successfully
- [ ] Verify preview renders

### Special Characters
- [ ] Title with emojis: `📝 My Note`
- [ ] Content with special chars: `<script>alert('test')</script>`
- [ ] Verify proper escaping in preview

## Test Results Summary

| Category | Tests Passed | Tests Failed | Notes |
|----------|--------------|--------------|-------|
| Registration | | | |
| Login | | | |
| Note Creation | | | |
| Note Editing | | | |
| Note Deletion | | | |
| Keyboard Shortcuts | | | |
| UI/UX | | | |
| Persistence | | | |
| Error Handling | | | |
| Security | | | |
| Multi-User | | | |
| Performance | | | |
| Edge Cases | | | |

## Issues Found

1. 
2. 
3. 

## Recommendations

1. 
2. 
3.
