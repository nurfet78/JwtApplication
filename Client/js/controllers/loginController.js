import { AuthApi } from '../api/authApi.js';
import { TokenService } from '../services/tokenService.js';
import { showMessage } from '../utils/messageUtil.js';

export class LoginController {
    constructor() {
        this.form = document.getElementById('loginForm');
        this.init();
    }

    init() {
        this.form.addEventListener('submit', this.handleSubmit.bind(this));
    }

    async handleSubmit(e) {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        try {
            const response = await AuthApi.login(username, password);
            
            if (response && response.accessToken) {
                TokenService.setTokens(
                    response.accessToken, 
                    response.refreshToken,
                    response.refreshTokenId
                );
                showMessage('Вход выполнен успешно!', 'success');
                
                const roles = TokenService.getRolesFromToken(response.accessToken);
                console.log('User roles:', roles);
                
                if (roles === 'ROLE_ADMIN') {
                    window.location.href = '/pages/admin/dashboard.html';
                } else {
                    window.location.href = '/pages/user/profile.html';
                }
            } else {
                showMessage('Неверный формат ответа от сервера', 'error');
            }
        } catch (error) {
            showMessage(error.message || 'Произошла ошибка при входе', 'error');
        }
    }
} 