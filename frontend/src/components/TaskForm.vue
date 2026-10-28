<script setup>
import { reactive } from 'vue'

const emit = defineEmits(['submit'])

const form = reactive({
  title: '',
  description: '',
})
function submit() {
  if (!form.title.trim()) return

  emit('submit', {
    title: form.title.trim(),
    description: form.description.trim(),
    completed: false,
  })
  form.title = ''
  form.description = ''
}
</script>

<template>
  <form class="task-form" @submit.prevent="submit">
    <div class="form-heading">
      <div>
        <p class="eyebrow">NEW TASK</p>
        <h2>할 일 추가</h2>
      </div>
      <span class="shortcut">⌘ + Enter</span>
    </div>

    <label>
      <span>제목</span>
      <input
        v-model="form.title"
        maxlength="120"
        placeholder="예: Kiro로 API 리팩터링"
        required
      />
    </label>

    <label>
      <span>설명</span>
      <textarea
        v-model="form.description"
        maxlength="500"
        rows="4"
        placeholder="작업의 배경이나 완료 조건을 적어 주세요."
        @keydown.meta.enter="submit"
        @keydown.ctrl.enter="submit"
      />
    </label>

    <button class="primary-button" type="submit" :disabled="!form.title.trim()">
      할 일 추가
      <span aria-hidden="true">↗</span>
    </button>
  </form>
</template>

