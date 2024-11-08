import { HttpClient, API_URL } from '../utils/httpClient.js';

export class RoleApi {
    static async getAllRoles() {
        try {
            const response = await HttpClient.fetch('/api/roles', {
                headers: {
                    'Accept': 'application/json'
                }
            });
            return response.json();
        } catch (error) {
            console.error('Error fetching roles:', error);
            throw error;
        }
    }
} 