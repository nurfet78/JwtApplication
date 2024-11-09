import { HttpClient } from '../utils/httpClient.js';

export class UserApi {
    static async getProfile() {
        try {
            const response = await HttpClient.fetch('/api/user/profile');
            return await response.json();
        } catch (error) {
            console.error('Error getting profile:', error);
            throw error;
        }
    }

    static async updateProfile(userData) {
        try {
            const response = await HttpClient.fetch('/api/user/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });

            return await response.json();
        } catch (error) {
            console.error('Error in updateProfile:', error);
            throw error;
        }
    }
} 