import { TokenService } from '../services/tokenService.js';
import { AuthApi } from '../api/authApi.js';
import { showMessage } from './messageUtil.js';

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

            console.log('Отправка запроса:', fullUrl);
            const response = await fetch(fullUrl, options);
            console.log('Получен ответ:', {
                status: response.status,
                statusText: response.statusText,
                headers: Object.fromEntries(response.headers.entries())
            });

            if (response.status === 401) {
                console.log('Получен статус 401 - необходима авторизация');
                TokenService.removeTokens();
                window.location.href = '/pages/login.html';
                showMessage('Сессия истекла. Пожалуйста, войдите снова.', 'error');
                throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
            } else if (response.status === 403) {
                console.log('Получен статус 403 - доступ запрещен');
                const errorData = await response.json();
                console.log('Сырые данные ошибки от сервера:', JSON.stringify(errorData, null, 2));
                
                let errorDetails = {
                    status: response.status,
                    error: 'Forbidden',
                    url: window.location.href,
                    timestamp: new Date().toLocaleString(),
                    details: errorData.Message ? errorData.Message.details : 'Access Denied'
                };
                
                sessionStorage.setItem('errorDetails', JSON.stringify(errorDetails));
                window.location.href = '/pages/errors/access-denied.html';
                throw new Error('Доступ запрещен');
            }

            if (!response.ok) {
                console.log('Получен неуспешный статус:', response.status);
                const errorText = await response.text();
                showMessage(errorText || 'Произошла ошибка', 'error');
                throw new Error(errorText || 'HTTP Error');
            }

            return response;

        } catch (error) {
            console.error('Ошибка HTTP запроса:', error);
            console.error('Стек вызовов:', error.stack);
            throw error;
        }
    }
} 