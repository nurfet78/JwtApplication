import { HttpClient, API_URL } from '../utils/httpClient.js';
import { TokenService } from '../services/tokenService.js';

export class AuthApi {
    static async login(username, password) {
        try {
            const response = await fetch(`${API_URL}/api/auth/signin`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Ошибка при входе');
            }

            return await response.json();

        } catch (error) {
            if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
                showMessage('Не удалось подключиться к серверу. Проверьте подключение к интернету.', 'danger');
                throw new Error('Не удалось подключиться к серверу. Проверьте подключение к интернету.');
            }
            throw error;
        }
    }

    static async register(userData) {
        const response = await fetch(`${API_URL}/api/auth/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const data = await response.json();
            if (!response.ok) {
                if (data.errors) {
                    throw { response: data };
                }
                throw new Error(data.message || 'Ошибка при регистрации');
            }
            return data;
        } else {
            const text = await response.text();
            if (!response.ok) {
                throw new Error(text || 'Ошибка при регистрации');
            }
            return { message: text, ok: response.ok };
        }
    }

    static async refreshToken(accessToken, refreshToken) {
        try {
            const response = await fetch(`${API_URL}/api/auth/refresh`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    accessToken,
                    refreshToken
                })
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || 'Ошибка при обновлении токенов');
            }

            return await response.json();
        } catch (error) {
            console.error('Ошибка при обновлении токенов:', error);
            throw error;
        }
    }

    static async getNewAccessToken(refreshToken) {
        try {
            const response = await fetch(`${API_URL}/api/auth/token`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    refreshToken
                })
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || 'Ошибка при получении нового токена');
            }

            return await response.json();
        } catch (error) {
            console.error('Ошибка при получении нового токена:', error);
            throw error;
        }
    }

    static async logout(refreshTokenId) {
        console.log('AuthApi.logout() called with refreshTokenId:', refreshTokenId);
        const response = await fetch(`${API_URL}/api/auth/logout`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${TokenService.getAccessToken()}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ refreshTokenId })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Server error response:', errorText);
            throw new Error(errorText);
        }

        const message = await response.text();
        console.log('Logout response:', message);
        return message;
    }
} 