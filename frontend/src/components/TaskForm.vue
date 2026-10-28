<script setup>
import { onMounted, reactive } from 'vue'

const props = defineProps({
  initialTask: { type: Object, default: null },
})

const emit = defineEmits(['submit'])

const form = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  dueDate: '',
})

onMounted(() => {
  if (props.initialTask) {
    form.priority = props.initialTask.priority || 'MEDIUM'
    form.dueDate = props.initialTask.dueDate || ''
  }
})

function submit() {
  if (!form.title.trim()) return

  emit('submit', {
    title: form.title.trim(),
    description: form.description.trim(),
    completed: false,
    priority: form.priority,
    dueDate: form.dueDate || null,
  })
  form.title = ''
  form.description = ''
  form.priority = 'MEDIUM'
  form.dueDate = ''
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

    <label>
      <span>우선순위</span>
      <select v-model="form.priority">
        <option value="LOW">낮음 (LOW)</option>
        <option value="MEDIUM">보통 (MEDIUM)</option>
        <option value="HIGH">높음 (HIGH)</option>
      </select>
    </label>

    <label>
      <span>마감일</span>
      <input type="date" v-model="form.dueDate" />
    </label>

    <button class="primary-button" type="submit" :disabled="!form.title.trim()">
      할 일 추가
      <span aria-hidden="true">↗</span>
    </button>
  </form>
</template>

