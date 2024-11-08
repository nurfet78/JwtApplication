import { AuthApi } from '../api/authApi.js';
import { showMessage } from '../utils/messageUtil.js';

export class RegisterController {
    constructor() {
        this.form = document.getElementById('registerForm');
        this.init();
    }

    init() {
        this.form.addEventListener('submit', this.handleSubmit.bind(this));
    }

    displayValidationErrors(errors) {
        this.clearValidationErrors();
        errors.forEach(error => {
            const field = error.field;
            const message = error.defaultMessage;
            
            const input = document.getElementById(field);
            if (input) {
                input.classList.add('is-invalid');
                
                const errorDiv = document.createElement('div');
                errorDiv.className = 'invalid-feedback';
                errorDiv.textContent = message;
                
                input.parentNode.appendChild(errorDiv);
            }
        });
    }

    clearValidationErrors() {
        document.querySelectorAll('.invalid-feedback').forEach(el => el.remove());
        document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    }

    async handleSubmit(e) {
        e.preventDefault();
        this.clearValidationErrors();

        const userData = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        };

        try {
            const response = await AuthApi.register(userData);
            
            if (response.errors) {
                this.displayValidationErrors(response.errors);
                return;
            }

            if (response.ok || response.message) {
                showMessage(response.message || 'Регистрация прошла успешно! Пожалуйста, войдите в систему.', 'success');
                setTimeout(() => {
                    window.location.href = '/pages/login.html';
                }, 2000);
            }
        } catch (error) {
            if (error.response && error.response.errors) {
                this.displayValidationErrors(error.response.errors);
            } else {
                showMessage(error.message || 'Произошла ошибка при регистрации', 'error');
                console.error('Registration error:', error);
            }
        }
    }
} 