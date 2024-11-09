import { API_URL } from '../utils/httpClient.js';
import { ErrorCodes } from '../constants/errorMessages.js';

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
                throw new Error('Не удалось подключиться к серверу. Проверьте подключение к интернету.');
            }
            throw error;
        }
    }

    static async register(userData) {
        try {
            const response = await fetch(`${API_URL}/api/auth/signup`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });

            if (!response.ok) {
                const errorText = await response.text();

                // Преобразуем текстовые сообщения в объекты с кодом ошибки
                if (errorText === 'Имя пользователя уже занято!') {
                    throw {
                        code: ErrorCodes.USERNAME_TAKEN,
                        message: errorText
                    };
                }

                if (errorText === 'Электронная почта уже используется!') {
                    throw {
                        code: ErrorCodes.EMAIL_TAKEN,
                        message: errorText
                    };
                }

                // Пытаемся распарсить JSON с ошибками валидации
                try {
                    const errorData = JSON.parse(errorText);
                    if (errorData.errors) {
                        throw { response: errorData };
                    }
                } catch (e) {
                    if (e.response) {
                        throw e;
                    }
                }

                throw new Error(errorText);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                const text = await response.text();
                return { ok: true, message: text };
            }
        } catch (error) {
            console.error('Registration error:', error);
            throw error;
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
}