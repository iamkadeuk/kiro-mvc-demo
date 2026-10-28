<script setup>
import { computed, reactive, ref, watch } from 'vue'

const props = defineProps({
  task: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['update', 'remove'])
const editing = ref(false)
const draft = reactive({ title: '', description: '', priority: 'MEDIUM', dueDate: '' })

watch(
  () => props.task,
  (task) => {
    draft.title = task.title
    draft.description = task.description || ''
    draft.priority = task.priority || 'MEDIUM'
    draft.dueDate = task.dueDate || ''
  },
  { immediate: true },
)

const createdDate = computed(() =>
  new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(props.task.createdAt)),
)

const dueDateFormatted = computed(() => {
  if (!props.task.dueDate) return null
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(new Date(props.task.dueDate))
})

function toggleCompleted() {
  emit('update', props.task.id, {
    title: props.task.title,
    description: props.task.description || '',
    completed: !props.task.completed,
    priority: props.task.priority,
    dueDate: props.task.dueDate || null,
  })
}

function save() {
  if (!draft.title.trim()) return
  emit('update', props.task.id, {
    title: draft.title.trim(),
    description: draft.description.trim(),
    completed: props.task.completed,
    priority: draft.priority,
    dueDate: draft.dueDate || null,
  })
  editing.value = false
}

function cancel() {
  draft.title = props.task.title
  draft.description = props.task.description || ''
  draft.priority = props.task.priority || 'MEDIUM'
  draft.dueDate = props.task.dueDate || ''
  editing.value = false
}
</script>

<template>
  <article class="task-card" :class="{ completed: task.completed, overdue: task.overdue }">
    <button
      class="check-button"
      type="button"
      :aria-label="task.completed ? '미완료로 변경' : '완료로 변경'"
      @click="toggleCompleted"
    >
      <span v-if="task.completed">✓</span>
    </button>

    <div class="task-content">
      <template v-if="editing">
        <input v-model="draft.title" class="edit-title" maxlength="120" />
        <textarea v-model="draft.description" class="edit-description" maxlength="500" rows="3" />
        <select v-model="draft.priority" class="edit-priority">
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
        <input v-model="draft.dueDate" type="date" class="edit-due-date" />
        <div class="edit-actions">
          <button type="button" class="text-button" @click="cancel">취소</button>
          <button type="button" class="save-button" @click="save">저장</button>
        </div>
      </template>

      <template v-else>
        <div class="task-meta">
          <span>{{ task.completed ? 'DONE' : 'IN PROGRESS' }}</span>
          <span class="dot">·</span>
          <span>{{ createdDate }}</span>
          <span class="dot">·</span>
          <span class="priority-label">{{ task.priority }}</span>
          <span v-if="dueDateFormatted" class="due-date">📅 {{ dueDateFormatted }}</span>
          <span v-if="task.overdue" class="overdue-label">기한 초과</span>
        </div>
        <h3>{{ task.title }}</h3>
        <p v-if="task.description">{{ task.description }}</p>
      </template>
    </div>

    <div v-if="!editing" class="card-actions">
      <button type="button" aria-label="수정" @click="editing = true">✎</button>
      <button type="button" aria-label="삭제" @click="emit('remove', task.id)">×</button>
    </div>
  </article>
</template>
