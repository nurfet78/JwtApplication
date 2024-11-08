import { HttpClient, API_URL } from '../utils/httpClient.js';

export class UserApi {
    static async getProfile() {
        try {
            const response = await HttpClient.fetch('/api/user/profile');
            const data = await response.json();
            
            // Проверяем, есть ли данные в ответе
            if (!data || !data.data) {
                console.error('Invalid profile data:', data);
                throw new Error('Неверный формат данных профиля');
            }
            
            return data;
        } catch (error) {
            console.error('Error in getProfile:', error);
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

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const data = await response.json();
                return data;
            } else {
                const text = await response.text();
                return { message: text, ok: response.ok };
            }
        } catch (error) {
            console.error('Error in updateProfile:', error);
            throw error;
        }
    }
} 