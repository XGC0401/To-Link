# Error Code Reference Guide

## Overview
This document describes all error codes used in the To-Link application. Each error has a unique code that helps identify the specific issue. Error codes are returned in the API response in the `code` field.

## Error Code Format
- **1xxx**: Authentication & Authorization errors
- **2xxx**: Validation errors  
- **3xxx**: Database errors
- **4xxx**: User Profile errors
- **5xxx**: Post errors
- **9xxx**: General/System errors
- **2xx**: Success codes

---

## Authentication & Authorization Errors (1xxx)

### 1001 - AUTH_INVALID_CREDENTIALS
**Message**: "Invalid email or password"
**Cause**: User entered incorrect email or password during login
**Solution**: Verify email and password are correct. Check for typos. Reset password if forgotten.

### 1002 - AUTH_EMAIL_ALREADY_EXISTS
**Message**: "Email is already registered"
**Cause**: Attempting to register with an email that already has an account
**Solution**: Use a different email address or login with existing account

### 1003 - AUTH_INVALID_EMAIL_FORMAT
**Message**: "Invalid email format"
**Cause**: Email address doesn't match valid email format
**Solution**: Enter a valid email address (e.g., user@example.com)

### 1004 - AUTH_PASSWORD_TOO_SHORT
**Message**: "Password must be at least 6 characters"
**Cause**: Password is too short during registration
**Solution**: Create a password with at least 6 characters

### 1005 - AUTH_USER_NOT_FOUND
**Message**: "User not found"
**Cause**: User account doesn't exist in the system
**Solution**: Register a new account or check email for typos

### 1006 - AUTH_ROLE_NOT_FOUND
**Message**: "User role not found in system"
**Cause**: System configuration issue - user roles not properly set up
**Solution**: Contact administrator

### 1007 - AUTH_TOKEN_INVALID
**Message**: "Invalid token"
**Cause**: JWT token is malformed or corrupted
**Solution**: Logout and login again to get a new token

### 1008 - AUTH_TOKEN_EXPIRED
**Message**: "Token has expired"
**Cause**: Session has expired after extended inactivity
**Solution**: Logout and login again

### 1009 - AUTH_UNAUTHORIZED
**Message**: "Unauthorized access"
**Cause**: User doesn't have permission to access resource
**Solution**: Contact administrator for access

### 1010 - AUTH_USERNAME_ALREADY_EXISTS
**Message**: "Username is already registered"
**Cause**: Attempting to register with a username that's already taken
**Solution**: Choose a different username

### 1011 - AUTH_MISSING_REQUIRED_FIELDS
**Message**: "Username, email, and password are required"
**Cause**: One or more required fields were not provided during registration
**Solution**: Fill in all required fields

---

## Validation Errors (2xxx)

### 2001 - VALIDATION_NAME_EMPTY
**Message**: "Username is required"
**Cause**: Name/Username field was left empty
**Solution**: Enter a valid username

### 2002 - VALIDATION_AGE_INVALID
**Message**: "Age is invalid"
**Cause**: Age value is outside acceptable range or invalid format
**Solution**: Enter a valid age between 0-150

### 2003 - VALIDATION_HKID_INVALID
**Message**: "HKID is invalid"
**Cause**: Hong Kong ID format is incorrect
**Solution**: Enter a valid HKID (e.g., S112233(4))

### 2004 - VALIDATION_ADDRESS_EMPTY
**Message**: "Address is required"
**Cause**: Address field was left empty
**Solution**: Enter a valid address

### 2005 - VALIDATION_EMAIL_INVALID
**Message**: "Invalid email format"
**Cause**: Email doesn't match valid format
**Solution**: Enter a valid email address

### 2006 - VALIDATION_PASSWORD_WEAK
**Message**: "Password must be at least 6 characters"
**Cause**: Password doesn't meet security requirements
**Solution**: Use a stronger password (min 6 characters)

---

## Database Errors (3xxx)

### 3001 - DATABASE_ERROR
**Message**: "Database error occurred"
**Cause**: General database operation failure
**Solution**: Retry the operation. If persists, contact administrator.

### 3002 - DATABASE_DUPLICATE_KEY
**Message**: "Record already exists"
**Cause**: Trying to create duplicate record with same unique key
**Solution**: Use different values or update existing record

### 3003 - DATABASE_RECORD_NOT_FOUND
**Message**: "Record not found in database"
**Cause**: Requested record doesn't exist
**Solution**: Verify record ID or search parameters

### 3004 - DATABASE_CONNECTION_ERROR
**Message**: "Unable to connect to database"
**Cause**: Database server is unavailable or unreachable
**Solution**: Check internet connection and retry. Contact administrator if persists.

---

## User Profile Errors (4xxx)

### 4001 - USER_PROFILE_NOT_FOUND
**Message**: "User profile not found"
**Cause**: User account doesn't exist or was deleted
**Solution**: Create a new account or contact support

### 4002 - USER_EMAIL_ALREADY_REGISTERED
**Message**: "Email is already registered"
**Cause**: Email is already associated with another account
**Solution**: Use a different email or recover existing account

### 4003 - USER_UPDATE_FAILED
**Message**: "Failed to update user profile"
**Cause**: Update operation encountered an error
**Solution**: Verify input data and retry. Contact support if persists.

### 4004 - USER_DELETE_FAILED
**Message**: "Failed to delete user account"
**Cause**: Deletion operation encountered an error
**Solution**: Retry or contact administrator

---

## Post Errors (5xxx)

### 5001 - POST_NOT_FOUND
**Message**: "Post not found"
**Cause**: Requested post doesn't exist or was deleted
**Solution**: Check post ID or browse available posts

### 5002 - POST_CREATE_FAILED
**Message**: "Failed to create post"
**Cause**: Post creation encountered an error
**Solution**: Verify post content and retry

### 5003 - POST_UPDATE_FAILED
**Message**: "Failed to update post"
**Cause**: Post update operation failed
**Solution**: Verify changes and retry

### 5004 - POST_DELETE_FAILED
**Message**: "Failed to delete post"
**Cause**: Post deletion encountered an error
**Solution**: Retry or contact administrator

### 5005 - POST_CONTENT_EMPTY
**Message**: "Post content is empty"
**Cause**: Post title or content is missing
**Solution**: Enter post content before submitting

### 5006 - POST_IMAGE_UPLOAD_FAILED
**Message**: "Failed to upload image"
**Cause**: Image upload encountered error (size, format, etc)
**Solution**: Check image format (JPG, PNG) and size (< 5MB)

---

## General/System Errors (9xxx)

### 9001 - INTERNAL_SERVER_ERROR
**Message**: "An internal server error occurred"
**Cause**: Unexpected server error occurred
**Solution**: Retry the operation. Contact administrator if persists.

### 9002 - BAD_REQUEST
**Message**: "Bad request"
**Cause**: Request format is invalid
**Solution**: Check request parameters and format

### 9003 - RESOURCE_NOT_FOUND
**Message**: "Resource not found"
**Cause**: Requested resource doesn't exist
**Solution**: Verify resource ID or path

### 9004 - REQUEST_TIMEOUT
**Message**: "Request timeout"
**Cause**: Server took too long to respond
**Solution**: Check internet connection and retry

---

## Success Codes (2xx)

### 200 - SUCCESS
**Message**: "Success"
**Cause**: Operation completed successfully
**Status**: ✓ No action needed

### 201 - CREATED
**Message**: "Created"
**Cause**: Resource was successfully created
**Status**: ✓ No action needed

### 202 - ACCEPTED
**Message**: "Accepted"
**Cause**: Request was accepted and is being processed
**Status**: ✓ No action needed

---

## How to Use Error Codes

### For Users
When you see an error message, note the error code (e.g., [1002]) and:
1. Read the solution above
2. Follow the recommended action
3. If error persists, contact support with the error code

### For Developers
When implementing error handling:
```javascript
try {
  // API call
} catch (error) {
  const errorCode = error.response?.data?.code;
  const errorMessage = error.response?.data?.message;
  console.log(`Error [${errorCode}]: ${errorMessage}`);
  
  // Handle specific errors
  if (errorCode === '1002') {
    // Handle email already exists
  }
}
```

### For Support Team
Use error codes to quickly identify issues:
- **1xxx**: Authentication problems
- **2xxx**: User input validation issues  
- **3xxx**: Database connectivity issues
- **4xxx**: User account issues
- **5xxx**: Content/Post issues
- **9xxx**: Server/System issues

---

## Common Error Scenarios

### Registration Failures
- **1002**: Email already registered → Use different email
- **1010**: Username already exists → Choose different username
- **2001-2006**: Validation errors → Check input format
- **1011**: Missing fields → Fill all required fields

### Login Failures
- **1001**: Wrong credentials → Verify email/password
- **1005**: User not found → Check email, may need to register
- **1007-1008**: Token issues → Logout and login again

### Profile Update Failures
- **4002**: Email already registered → Use different email
- **4003**: Update failed → Retry or contact support
- **2005**: Invalid email format → Enter valid email

---

## Support Contact
If you continue to experience errors after following the solutions:
- Email: support@to-link.com
- Provide error code, error message, and steps to reproduce
- Include browser/device information if applicable
