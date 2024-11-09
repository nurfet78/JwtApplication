import { AuthApi } from '../api/authApi.js';
import { ValidationService } from '../services/validationService.js';
import { showMessage } from '../utils/messageUtil.js';
import { ErrorCodes } from '../constants/errorMessages.js';

export class RegisterController {
    constructor() {
        this.form = document.getElementById('registerForm');
        this.init();
    }

    init() {
        this.form.addEventListener('submit', this.handleSubmit.bind(this));
    }

    async handleSubmit(e) {
        e.preventDefault();
        ValidationService.clearValidationErrors();

        const userData = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        };

        try {
            console.log('Sending registration data:', userData);
            const response = await AuthApi.register(userData);

            if (response.ok) {
                showMessage('Регистрация прошла успешно! Пожалуйста, войдите в систему.', 'success');
                setTimeout(() => {
                    window.location.href = '/pages/login.html';
                }, 2000);
            }
        } catch (error) {
            console.log('Registration error:', error);
            if (error.response && error.response.errors) {
                ValidationService.displayValidationErrors(error.response.errors);
            } else if (error.code === ErrorCodes.USERNAME_TAKEN) {
                console.log('Username taken error');
                ValidationService.displayValidationErrors([{
                    field: 'username',
                    defaultMessage: error.message
                }]);
            } else if (error.code === ErrorCodes.EMAIL_TAKEN) {
                console.log('Email taken error');
                ValidationService.displayValidationErrors([{
                    field: 'email',
                    defaultMessage: error.message
                }]);
            } else {
                showMessage(error.message || 'Произошла ошибка при регистрации', 'error');
            }
        }
    }
}