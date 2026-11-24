// Shared API Utility & Toast System

const API = {
    async request(endpoint, options = {}) {
        try {
            const response = await fetch(endpoint, options);
            const isJson = response.headers.get('content-type')?.includes('application/json');
            const data = isJson ? await response.json() : null;

            if (!response.ok) {
                const errorMsg = data?.detail || 'An error occurred';
                if (response.status === 401 || response.status === 403) {
                    // Do nothing here for admin auth check, handle in admin.js
                }
                throw new Error(errorMsg);
            }
            return data;
        } catch (error) {
            Toast.show(error.message, 'error');
            throw error;
        }
    },

    get: (endpoint) => API.request(endpoint),
    post: (endpoint, body) => API.request(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    }),
    postForm: (endpoint, formData) => API.request(endpoint, {
        method: 'POST',
        body: formData // Let browser set Content-Type for multipart
    }),
    patch: (endpoint, body) => API.request(endpoint, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
};

const Toast = {
    container: null,
    init() {
        this.container = document.createElement('div');
        this.container.id = 'toast-container';
        document.body.appendChild(this.container);
    },
    show(message, type = 'success') {
        if (!this.container) this.init();
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        this.container.appendChild(toast);
        setTimeout(() => toast.remove(), 4500);
    }
};

document.addEventListener('DOMContentLoaded', () => Toast.init());
