import { AuthApi } from '../api/authApi.js';
import { TokenService } from '../services/tokenService.js';
import { showMessage } from '../utils/messageUtil.js';
import { ValidationService } from '../services/validationService.js';

export class LoginController {
    constructor() {
        this.form = document.getElementById('loginForm');
        this.init();
    }

    init() {
        this.form.addEventListener('submit', this.handleSubmit.bind(this));

        // Добавляем обработчики для валидации при вводе
        const inputs = this.form.querySelectorAll('input[required]');
        inputs.forEach(input => {
            input.addEventListener('input', () => {
                if (input.value.trim()) {
                    input.classList.remove('is-invalid');
                }
            });
        });
    }

    async handleSubmit(e) {
        e.preventDefault();
        ValidationService.clearFormValidationErrors(this.form);

        const username = document.getElementById('username');
        const password = document.getElementById('password');
        let isValid = true;

        if (!username.value.trim()) {
            username.classList.add('is-invalid');
            isValid = false;
        }

        if (!password.value.trim()) {
            password.classList.add('is-invalid');
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        try {
            const response = await AuthApi.login(username.value, password.value);

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
            showMessage(error.message, 'danger');
        }
    }
} 