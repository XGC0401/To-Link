import { ref, Ref } from 'vue'

/**
 * Composable for handling Enter key navigation in forms
 * Focuses on the next field when Enter is pressed on non-last field
 * Calls submit callback on the last field
 */
export const useFormEnter = () => {
  const handleFormEnter = (fieldRefs: Ref<any>[], fieldIndex: number, onSubmit: () => void) => {
    if (fieldIndex < fieldRefs.length - 1) {
      // Focus next field
      const nextRef = fieldRefs[fieldIndex + 1]
      if (nextRef.value?.focus) {
        nextRef.value.focus()
      }
    } else {
      // Submit on last field
      onSubmit()
    }
  }

  return {
    handleFormEnter
  }
}
