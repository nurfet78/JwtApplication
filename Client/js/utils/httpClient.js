import { TokenService } from '../services/tokenService.js';
import { AuthApi } from '../api/authApi.js';
import { ErrorCodes } from '../constants/errorMessages.js';

export const API_URL = 'http://localhost:8080';

export class HttpClient {
    static async fetch(url, options = {}) {
        const fullUrl = `${API_URL}${url}`;

        if (!options.headers) {
            options.headers = {};
        }

        try {
            const currentToken = TokenService.getAccessToken();
            if (currentToken) {
                options.headers['Authorization'] = `Bearer ${currentToken}`;
            }

            if (TokenService.isAccessTokenExpired()) {
                const refreshTokenRemaining = TokenService.getRefreshTokenRemainingTime();
                console.log('Оставшееся время refresh токена:', refreshTokenRemaining + '%');

                if (refreshTokenRemaining <= 33) {
                    console.log('Обновление пары токенов...');
                    const response = await AuthApi.refreshToken(
                        TokenService.getAccessToken(),
                        TokenService.getRefreshToken()
                    );
                    console.log('Получена новая пара токенов');
                    TokenService.setTokens(response.accessToken, response.refreshToken);
                    options.headers['Authorization'] = `Bearer ${response.accessToken}`;
                } else {
                    console.log('Обновление access токена...');
                    const response = await AuthApi.getNewAccessToken(TokenService.getRefreshToken());
                    console.log('Получен новый access токен');
                    TokenService.setAccessToken(response.accessToken);
                    options.headers['Authorization'] = `Bearer ${response.accessToken}`;
                }
            }

            const response = await fetch(fullUrl, options);
            console.log('Response status:', response.status);

            if (!response.ok) {
                const errorText = await response.text();
                console.log('Error text:', errorText);

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

            return response;

        } catch (error) {
            console.error('HTTP Client Error:', error);
            throw error;
        }
    }
}