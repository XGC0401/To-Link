# To-Link Testing & QA Checklist

## 🧪 Manual Testing Checklist

### Authentication & Profile
- [ ] User can change password successfully
  - [ ] Validates minimum 6 characters
  - [ ] Requires password confirmation match
  - [ ] Shows error for incorrect current password
  - [ ] Shows success message after change
  - [ ] User can login with new password

- [ ] Profile page displays correctly
  - [ ] Profession field shows (read-only)
  - [ ] Bio/description has word-limit counter
  - [ ] Counter styling is transparent
  - [ ] Form state persists to localStorage

### Posts & Editing
- [ ] Posts display with like button
  - [ ] Like button toggles between liked/unliked
  - [ ] Like count increments/decrements correctly
  - [ ] Like state persists across page reload
  - [ ] Like button shows red when active (#dc2626)

- [ ] Post editing works correctly
  - [ ] All 13 categories visible in dropdown
  - [ ] Custom category field shows only for "Other"
  - [ ] Images can be uploaded and previewed
  - [ ] Edit saves all fields (title, content, category, images)
  - [ ] Edited post reflects changes in feed

- [ ] Post creation works
  - [ ] Can create post without duplicate
  - [ ] Images preview before upload
  - [ ] Form validation shows required fields
  - [ ] Post appears in feed after creation

### Quests
- [ ] Quest creation page loads
  - [ ] Title and description fields present
  - [ ] Tag input validates properly
  - [ ] Images upload (max 5)
  - [ ] Payment method selector works
  - [ ] Reward points field accepts numbers

- [ ] Quest form submission
  - [ ] Validates required fields
  - [ ] Saves to localStorage with ID
  - [ ] Redirects to /posts on success
  - [ ] Quest appears in quest list

- [ ] Quest editing
  - [ ] Edit dialog shows all quest data
  - [ ] Can modify all fields
  - [ ] Dialog is centered on screen
  - [ ] Changes save correctly

### Friends
- [ ] Friends list displays
  - [ ] Shows current friends
  - [ ] Can search/filter friends

- [ ] Discover people panel works
  - [ ] Shows list of other users
  - [ ] Search filters results by name
  - [ ] Add friend button works
  - [ ] Added user appears in friends list
  - [ ] Success message shows

### Backpack & Rewards
- [ ] Backpack displays redeemed items
  - [ ] Shows all items with details
  - [ ] Search/filter functionality works

- [ ] Use item flow works
  - [ ] Clicking active item shows confirmation
  - [ ] Confirm marks item as used
  - [ ] Item moves to history
  - [ ] Used item shows correct status/styling

- [ ] History view works
  - [ ] Toggle shows used/expired items
  - [ ] Status displays correctly (used/expired)
  - [ ] Items styled appropriately

- [ ] Expiration tracking
  - [ ] New items get expiresAt (now + 30 days)
  - [ ] Expired items show in history
  - [ ] Can toggle between active and history

### Dialogs & UI
- [ ] All dialogs center on screen
  - [ ] Password change dialog
  - [ ] Edit post dialog
  - [ ] Edit quest dialog
  - [ ] Backpack items dialog
  - [ ] Admin delete dialog
  - [ ] Create group chat dialog
  - [ ] Image preview dialogs

- [ ] Dialog responsive on mobile
  - [ ] Max-width adjusts for small screens
  - [ ] Scrollable body for long content
  - [ ] Still readable at 320px width

- [ ] Login page displays correctly
  - [ ] Brand title/description centered
  - [ ] Language switcher button styled
  - [ ] Gradient background on button
  - [ ] Language change works

### Chat
- [ ] Chat page loads
  - [ ] Conversation list displays
  - [ ] Message history visible
  - [ ] Can send text messages

- [ ] Message types supported
  - [ ] Text messages
  - [ ] Photo sharing
  - [ ] Video sharing
  - [ ] Document sharing
  - [ ] Audio sharing
  - [ ] Location sharing

- [ ] User interactions
  - [ ] Can block/unblock user
  - [ ] Block status shows correctly
  - [ ] Create group chat works

### Admin Functions
- [ ] Admin delete dialog
  - [ ] Reason tags visible
  - [ ] Description textarea present
  - [ ] Confirmation phrase validation
  - [ ] Delete button only enabled when complete
  - [ ] Delete action works

### Localization (i18n)
- [ ] English language works
  - [ ] All strings show in English
  - [ ] No missing translation keys

- [ ] Traditional Chinese works
  - [ ] Language toggle switches to 中文
  - [ ] All strings translated
  - [ ] No untranslated keys

- [ ] New keys present
  - [ ] Password change strings
  - [ ] Friend discovery strings
  - [ ] Backpack strings
  - [ ] All admin strings

### Performance & Storage
- [ ] localStorage persistence
  - [ ] Like counts saved
  - [ ] Friend list persists
  - [ ] Form data saved
  - [ ] No localStorage overflow errors

- [ ] API communication
  - [ ] Requests use correct JWT token
  - [ ] Responses parse correctly
  - [ ] Error messages display
  - [ ] Success messages show

- [ ] Build performance
  - [ ] Frontend builds in reasonable time
  - [ ] No console errors in browser
  - [ ] No memory leaks on navigation

---

## 🔍 Code Quality Checks

- [ ] No TypeScript errors
- [ ] No Vue linter warnings
- [ ] No unused imports
- [ ] No console.log statements (except debug)
- [ ] Proper error handling
- [ ] All i18n keys defined
- [ ] localStorage keys documented
- [ ] API contracts match

---

## ✅ Validation Checklist

- [ ] Frontend builds successfully
- [ ] Backend compiles without errors
- [ ] No missing dependencies
- [ ] All API endpoints responding
- [ ] Database migrations applied
- [ ] JWT token validation working
- [ ] CORS configured correctly

---

## 📱 Browser Compatibility

Test on:
- [ ] Chrome/Edge (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Mobile Safari (iOS)
- [ ] Chrome Mobile (Android)

---

## 🚀 Deployment Readiness

Before production:
- [ ] All tests passing
- [ ] Performance metrics acceptable
- [ ] Security audit completed
- [ ] Accessibility checked
- [ ] Database backups in place
- [ ] Rollback plan ready
- [ ] Monitoring configured
- [ ] Documentation reviewed

---

## 📝 Notes

- Use browser DevTools Console to check for errors
- Use Network tab to verify API calls
- Use Storage tab to inspect localStorage
- Test with dark mode enabled/disabled
- Test with different screen sizes
- Clear browser cache between tests

---

**Estimated Testing Time**: 2-3 hours for thorough QA
**Critical Path**: Auth → Posts → Friends → Backpack
**Priority**: All items marked with ⚠️ should be tested first

