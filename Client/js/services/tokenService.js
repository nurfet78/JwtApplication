import { showMessage } from '../utils/messageUtil.js';
import { API_URL } from '../utils/httpClient.js';

export class TokenService {
    static getAccessToken() {
        return localStorage.getItem('accessToken');
    }

    static getRefreshToken() {
        return localStorage.getItem('refreshToken');
    }

    static setTokens(accessToken, refreshToken, refreshTokenId) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        if (refreshTokenId) {
            localStorage.setItem('refreshTokenId', refreshTokenId);
        }
    }

    static removeTokens() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('refreshTokenId');
    }

    static isAccessTokenExpired() {
        const token = this.getAccessToken();
        if (!token) return true;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000);
            return payload.exp < currentTime;
        } catch {
            return true;
        }
    }

    static getRefreshTokenRemainingTime() {
        const token = this.getRefreshToken();
        if (!token) return 0;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000);
            const timeUntilExpiry = payload.exp - currentTime;
            const totalLifetime = payload.exp - payload.iat;
            const remainingLifetimePercent = (timeUntilExpiry / totalLifetime) * 100;

            return remainingLifetimePercent;
        } catch {
            return 0;
        }
    }

    static setAccessToken(accessToken) {
        localStorage.setItem('accessToken', accessToken);
    }

    static getRolesFromToken(token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.roles;
        } catch (error) {
            return null;
        }
    }

    static async logout() {
        try {
            const refreshTokenId = this.getRefreshTokenId();
            if (!refreshTokenId) {
                console.error('RefreshTokenId not found');
                throw new Error('RefreshTokenId not found');
            }

            const response = await fetch(`${API_URL}/api/auth/logout`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.getAccessToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ refreshTokenId })
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText);
            }

            const message = await response.text();
            console.log('Logout response:', message);
            showMessage(message, 'info');

        } catch (error) {
            console.error('Error during logout:', error);
        } finally {
            // В любом случае удаляем токены
            this.removeTokens();
        }
    }

    static getRefreshTokenId() {
        return localStorage.getItem('refreshTokenId');
    }

    static getUserIdFromToken(token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.id;
        } catch (error) {
            console.error('Error parsing token:', error);
            return null;
        }
    }
} 