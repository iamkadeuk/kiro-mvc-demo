<script setup>
import { computed, onMounted, ref } from 'vue'
import TaskCard from './components/TaskCard.vue'
import TaskForm from './components/TaskForm.vue'
import { taskApi } from './services/taskApi'

const tasks = ref([])
const loading = ref(true)
const error = ref('')
const filter = ref('all')

const completedCount = computed(() => tasks.value.filter((task) => task.completed).length)
const progress = computed(() =>
  tasks.value.length ? Math.round((completedCount.value / tasks.value.length) * 100) : 0,
)
const filteredTasks = computed(() => {
  if (filter.value === 'active') return tasks.value.filter((task) => !task.completed)
  if (filter.value === 'done') return tasks.value.filter((task) => task.completed)
  return tasks.value
})

async function loadTasks() {
  loading.value = true
  error.value = ''
  try {
    tasks.value = await taskApi.findAll()
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}

async function createTask(task) {
  error.value = ''
  try {
    const created = await taskApi.create(task)
    tasks.value.unshift(created)
  } catch (requestError) {
    error.value = requestError.message
  }
}

async function updateTask(id, task) {
  error.value = ''
  try {
    const updated = await taskApi.update(id, task)
    const index = tasks.value.findIndex((item) => item.id === id)
    tasks.value[index] = updated
  } catch (requestError) {
    error.value = requestError.message
  }
}

async function removeTask(id) {
  error.value = ''
  try {
    await taskApi.remove(id)
    tasks.value = tasks.value.filter((task) => task.id !== id)
  } catch (requestError) {
    error.value = requestError.message
  }
}

onMounted(loadTasks)
</script>

<template>
  <div class="app-shell">
    <header class="site-header">
      <a class="brand" href="#" aria-label="Kiro MVC 홈">
        <span class="brand-mark">K</span>
        <span>Kiro MVC</span>
      </a>
      <div class="tech-stack">
        <span>Vue 3</span>
        <span>Spring Boot</span>
        <span>H2</span>
      </div>
    </header>

    <main>
      <section class="hero">
        <div class="hero-copy">
          <p class="eyebrow">MVC PATTERN LAB</p>
          <h1>아이디어를<br /><em>작업으로.</em></h1>
          <p class="hero-description">
            Vue의 View와 Spring Boot의 Controller·Service·Repository 계층을 한눈에 따라가는
            작은 CRUD 예제입니다.
          </p>
        </div>

        <div class="progress-card">
          <div class="progress-number">{{ progress }}<small>%</small></div>
          <div class="progress-copy">
            <strong>오늘의 진행률</strong>
            <span>{{ completedCount }} / {{ tasks.length }} 완료</span>
          </div>
          <div class="progress-track" aria-hidden="true">
            <div :style="{ width: `${progress}%` }"></div>
          </div>
        </div>
      </section>

      <section class="workspace">
        <TaskForm @submit="createTask" />

        <div class="task-panel">
          <div class="panel-header">
            <div>
              <p class="eyebrow">TASK BOARD</p>
              <h2>작업 목록</h2>
            </div>
            <div class="filters" aria-label="작업 필터">
              <button
                v-for="option in [
                  { value: 'all', label: '전체' },
                  { value: 'active', label: '진행 중' },
                  { value: 'done', label: '완료' },
                ]"
                :key="option.value"
                type="button"
                :class="{ active: filter === option.value }"
                @click="filter = option.value"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <p v-if="error" class="error-message" role="alert">
            {{ error }}
            <button type="button" @click="loadTasks">다시 시도</button>
          </p>

          <div v-if="loading" class="empty-state">작업을 불러오는 중입니다…</div>
          <div v-else-if="filteredTasks.length === 0" class="empty-state">
            이 조건에 맞는 작업이 없습니다.
          </div>
          <div v-else class="task-list">
            <TaskCard
              v-for="task in filteredTasks"
              :key="task.id"
              :task="task"
              @update="updateTask"
              @remove="removeTask"
            />
          </div>
        </div>
      </section>

      <section class="architecture">
        <p class="eyebrow">REQUEST FLOW</p>
        <div class="flow">
          <div><span>01</span><strong>Vue View</strong><small>사용자 입력</small></div>
          <i>→</i>
          <div><span>02</span><strong>Controller</strong><small>HTTP 요청</small></div>
          <i>→</i>
          <div><span>03</span><strong>Service</strong><small>비즈니스 규칙</small></div>
          <i>→</i>
          <div><span>04</span><strong>Repository</strong><small>데이터 저장</small></div>
        </div>
      </section>
    </main>
  </div>
</template>

