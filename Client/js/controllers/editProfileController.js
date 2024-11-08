import { UserApi } from '../api/userApi.js';
import { TokenService } from '../services/tokenService.js';
import { showMessage } from '../utils/messageUtil.js';

export class EditProfileController {
    constructor() {
        this.form = document.getElementById('editProfileForm');
        this.init();
        this.setupLogout();
        this.originalUsername = '';
    }

    async init() {
        if (!TokenService.getAccessToken()) {
            window.location.href = '/pages/login.html';
            return;
        }
        await this.loadProfile();
        this.setupFormSubmit();
    }

    setupLogout() {
        document.getElementById('logoutBtn').addEventListener('click', () => {
            TokenService.removeTokens();
            window.location.href = '/pages/login.html';
        });
    }

    setupFormSubmit() {
        this.form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.updateProfile();
        });
    }

    async loadProfile() {
        try {
            const response = await UserApi.getProfile();
            if (response.data) {
                const data = response.data;
                if (data.id) {
                    document.getElementById('userId').value = data.id;
                }
                document.getElementById('firstName').value = data.firstName;
                document.getElementById('lastName').value = data.lastName;
                document.getElementById('email').value = data.email;
                document.getElementById('username').value = data.username;
                this.originalUsername = data.username;
            }
        } catch (error) {
            showMessage('Ошибка загрузки профиля', 'error');
            console.error('Error loading profile:', error);
        }
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

    async updateProfile() {
        try {
            this.clearValidationErrors();

            const userId = document.getElementById('userId').value;
            if (!userId) {
                showMessage('Ошибка: ID пользователя не найден', 'error');
                return;
            }

            const newUsername = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const userData = {
                id: parseInt(userId),
                firstName: document.getElementById('firstName').value,
                lastName: document.getElementById('lastName').value,
                email: document.getElementById('email').value,
                username: newUsername,
                password: password
            };

            if (!userData.password) {
                delete userData.password;
            }

            const response = await UserApi.updateProfile(userData);
            
            if (response.errors) {
                this.displayValidationErrors(response.errors);
                return;
            }

            showMessage('Профиль успешно обновлен', 'success');
            
            if (password || newUsername !== this.originalUsername) {
                showMessage('Данные авторизации изменены. Пожалуйста, войдите снова', 'info');
                setTimeout(() => {
                    TokenService.removeTokens();
                    window.location.href = '/pages/login.html';
                }, 2000);
            } else {
                setTimeout(() => {
                    window.location.href = '/pages/user/profile.html';
                }, 1500);
            }
            
        } catch (error) {
            if (error.response && error.response.errors) {
                this.displayValidationErrors(error.response.errors);
            } else {
                showMessage('Ошибка при обновлении профиля', 'error');
                console.error('Error updating profile:', error);
            }
        }
    }
} 