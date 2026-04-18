// api.js

const API_BASE_URL = window.location.origin === 'http://localhost:8080' 
    ? 'http://localhost:8080' 
    : '';

// Centralised config for tokens
const tokenManager = {
    getToken: () => localStorage.getItem('token'),
    setToken: (token) => localStorage.setItem('token', token),
    clearToken: () => localStorage.removeItem('token'),
    isAuthenticated: () => !!localStorage.getItem('token'),
    getRole: () => {
        const token = localStorage.getItem('token');
        if (!token) return null;
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.role;
        } catch {
            return null;
        }
    }
};

// Generic fetch wrapper that adds Auth headers and handles JSON
async function apiFetch(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (tokenManager.isAuthenticated()) {
        headers['Authorization'] = `Bearer ${tokenManager.getToken()}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(url, config);
        
        let data;
        // Not all responses have a JSON body (e.g. DELETE might return plain text)
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            data = await response.json();
        } else {
            data = await response.text();
        }
        
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                // If unauthorized and we're not already on login
                if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('register.html')) {
                   tokenManager.clearToken();
                   window.location.href = '/login.html';
                }
            }
            throw new Error(data.message || data || 'An error occurred');
        }

        return data;
    } catch (error) {
        throw error;
    }
}

// User API module
const UserAPI = {
    login: async (email, password) => {
        return apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    register: async (userData) => {
        // userData requires: username, email, password, fullName
        return apiFetch('/api/users', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    },

    getProfile: async () => {
        return apiFetch('/api/users/me', {
            method: 'GET'
        });
    },

    updateProfile: async (userData) => {
         // userData requires: fullName, username
        return apiFetch('/api/users/me', {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
    },

    deactivateAccount: async () => {
        return apiFetch('/api/users/me', {
            method: 'DELETE'
        });
    },
    
    getAllUsers: async () => {
        return apiFetch('/api/users/admin/all', {
            method: 'GET'
        });
    },

    getPublicUser: async (id) => {
        return apiFetch(`/api/users/public/${id}`, {
            method: 'GET'
        });
    },

    searchUsers: async (query) => {
        return apiFetch(`/api/users/search?query=${encodeURIComponent(query)}`, {
            method: 'GET'
        });
    },

    deleteUser: async (id) => {
        return apiFetch(`/api/users/admin/${id}`, {
            method: 'DELETE'
        });
    }
};

// Project API module
const ProjectAPI = {
    getAll: async () => {
        return apiFetch('/api/projects', { method: 'GET' });
    },

    getById: async (id) => {
        return apiFetch(`/api/projects/${id}`, { method: 'GET' });
    },

    create: async (data) => {
        return apiFetch('/api/projects', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    update: async (id, data) => {
        return apiFetch(`/api/projects/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete: async (id) => {
        return apiFetch(`/api/projects/${id}`, { method: 'DELETE' });
    },

    addMember: async (projectId, userId) => {
        return apiFetch(`/api/projects/${projectId}/members/${userId}`, { method: 'POST' });
    },

    removeMember: async (projectId, userId) => {
        return apiFetch(`/api/projects/${projectId}/members/${userId}`, { method: 'DELETE' });
    },

    getMembers: async (projectId) => {
        return apiFetch(`/api/projects/${projectId}/members`, { method: 'GET' });
    },

    getMessages: async (projectId) => {
        return apiFetch(`/api/projects/${projectId}/messages`, { method: 'GET' });
    },

    sendMessage: async (projectId, content) => {
        return apiFetch(`/api/projects/${projectId}/messages`, {
            method: 'POST',
            body: JSON.stringify({ content })
        });
    },

    generateRoadmap: async (projectId) => {
        return apiFetch(`/api/projects/${projectId}/generate-roadmap`, { method: 'POST' });
    }
};

// Roadmap API module
const RoadmapAPI = {
    getRoadmap: async (projectId) => {
        return apiFetch(`/api/projects/${projectId}/roadmap`, { method: 'GET' });
    },
    updateStatus: async (projectId, itemId, status) => {
        return apiFetch(`/api/projects/${projectId}/roadmap/${itemId}`, {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
    }
};

// Issue API module
const IssueAPI = {
    getByProject: async (projectId) => {
        return apiFetch(`/api/projects/${projectId}/issues`, { method: 'GET' });
    },

    getById: async (projectId, issueId) => {
        return apiFetch(`/api/projects/${projectId}/issues/${issueId}`, { method: 'GET' });
    },

    create: async (projectId, data) => {
        return apiFetch(`/api/projects/${projectId}/issues`, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    update: async (projectId, issueId, data) => {
        return apiFetch(`/api/projects/${projectId}/issues/${issueId}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete: async (projectId, issueId) => {
        return apiFetch(`/api/projects/${projectId}/issues/${issueId}`, { method: 'DELETE' });
    },

    updateStatus: async (projectId, issueId, status) => {
        return apiFetch(`/api/projects/${projectId}/issues/${issueId}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });
    },

    assign: async (projectId, issueId, assigneeId) => {
        return apiFetch(`/api/projects/${projectId}/issues/${issueId}/assign?assigneeId=${assigneeId}`, { method: 'PATCH' });
    }
};

// UI Helpers
const UI = {
    showAlert: (elementId, message, type = 'error') => {
        const el = document.getElementById(elementId);
        if (el) {
            el.textContent = message;
            el.className = `alert alert-${type}`;
            el.style.display = 'block';
            setTimeout(() => {
                el.style.display = 'none';
            }, 5000);
        }
    },
    
    logout: () => {
        tokenManager.clearToken();
        window.location.href = '/login.html';
    },

    initNavbar: () => {
        const navbar = document.getElementById('navbar-container');
        if (!navbar) return;

        if (tokenManager.isAuthenticated()) {
            const role = tokenManager.getRole();
            let navLinks = '';
            
            if (role === 'ADMIN') {
                navLinks = `
                    <a href="/admin.html">Admin Dashboard</a>
                    <a href="#" onclick="UI.logout(); return false;">Logout</a>
                `;
            } else {
                navLinks = `
                    <a href="/home.html">Home</a>
                    <a href="/projects.html">Projects</a>
                    <a href="/profile.html">Profile</a>
                    <a href="#" onclick="UI.logout(); return false;">Logout</a>
                `;
            }

            navbar.innerHTML = `
                <nav class="navbar">
                    <div class="container nav-content">
                        <h2>PaITS</h2>
                        <div class="nav-links">
                            ${navLinks}
                        </div>
                    </div>
                </nav>
            `;
        } else {
             navbar.innerHTML = `
                <nav class="navbar">
                    <div class="container nav-content">
                        <h2>PaITS</h2>
                        <div class="nav-links">
                            <a href="/login.html">Login</a>
                            <a href="/register.html">Register</a>
                        </div>
                    </div>
                </nav>
            `;
        }
    }
};

// Auto-init navbar if container exists
document.addEventListener('DOMContentLoaded', () => {
    UI.initNavbar();
});
