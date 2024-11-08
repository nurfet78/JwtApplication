import { HttpClient, API_URL } from '../utils/httpClient.js';
import { TokenService } from '../services/tokenService.js';

export class AdminApi {
    static async getAllUsers() {
        const response = await HttpClient.fetch('/api/admin/users');
        return response.json();
    }

    static async getUser(id) {
        const response = await HttpClient.fetch(`/api/admin/editUser/${id}`);
        return response.json();
    }

    static async updateUser(userData) {
        const response = await HttpClient.fetch('/api/admin/editUser', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const data = await response.json();
            return data;
        } else {
            const text = await response.text();
            return { message: text, ok: response.ok };
        }
    }

    static async deleteUser(id) {
        const response = await HttpClient.fetch(`/api/admin/deleteUser/${id}`, {
            method: 'DELETE'
        });
        return response;
    }

    static async revokeRefreshToken(userId) {
        console.log('Revoking refresh token for user:', userId);
        const response = await HttpClient.fetch(`/api/admin/revokeRefreshToken/${userId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

        const message = await response.text();
        console.log('Revoke response:', message);
        return message;
    }
} 