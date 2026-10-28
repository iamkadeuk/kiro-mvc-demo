const BASE_URL = '/api/tasks'

async function request(url = '', options = {}) {
  const response = await fetch(`${BASE_URL}${url}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail || '요청을 처리하지 못했습니다.')
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

export const taskApi = {
  findAll() {
    return request()
  },

  create(task) {
    return request('', {
      method: 'POST',
      body: JSON.stringify(task),
    })
  },

  update(id, task) {
    return request(`/${id}`, {
      method: 'PUT',
      body: JSON.stringify(task),
    })
  },

  remove(id) {
    return request(`/${id}`, { method: 'DELETE' })
  },
}

