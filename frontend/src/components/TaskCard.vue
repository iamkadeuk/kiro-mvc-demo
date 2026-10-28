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
const draft = reactive({ title: '', description: '' })

watch(
  () => props.task,
  (task) => {
    draft.title = task.title
    draft.description = task.description || ''
  },
  { immediate: true },
)

const createdDate = computed(() =>
  new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(props.task.createdAt)),
)

function toggleCompleted() {
  emit('update', props.task.id, {
    title: props.task.title,
    description: props.task.description || '',
    completed: !props.task.completed,
  })
}

function save() {
  if (!draft.title.trim()) return
  emit('update', props.task.id, {
    title: draft.title.trim(),
    description: draft.description.trim(),
    completed: props.task.completed,
  })
  editing.value = false
}

function cancel() {
  draft.title = props.task.title
  draft.description = props.task.description || ''
  editing.value = false
}
</script>

<template>
  <article class="task-card" :class="{ completed: task.completed }">
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
