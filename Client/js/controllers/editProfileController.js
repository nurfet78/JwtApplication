import { UserApi } from '../api/userApi.js';
import { TokenService } from '../services/tokenService.js';
import { showMessage } from '../utils/messageUtil.js';
import { ValidationService } from '../services/validationService.js';
import { ErrorCodes, ErrorFields } from '../constants/errorMessages.js';

export class EditProfileController {
    constructor() {
        this.form = document.getElementById('editProfileForm');
        this.init();
        this.setupLogout();
        this.originalUsername = '';
        this.userId = null;
    }

    async init() {
        if (!TokenService.getAccessToken()) {
            window.location.href = '/pages/login.html';
            return;
        }
        await this.loadProfile();
        this.setupForm();
    }

    setupForm() {
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
                this.userId = data.id;
                document.getElementById('userId').value = data.id;
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

    async updateProfile() {
        try {
            ValidationService.clearValidationErrors();

            const userData = {
                id: this.userId,
                firstName: document.getElementById('firstName').value,
                lastName: document.getElementById('lastName').value,
                email: document.getElementById('email').value,
                username: document.getElementById('username').value,
                password: document.getElementById('password').value
            };

            if (!userData.password) {
                delete userData.password;
            }

            const response = await UserApi.updateProfile(userData);

            showMessage('Профиль успешно обновлен', 'success');

            const newUsername = userData.username;
            const password = userData.password;

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
            console.error('Error updating profile:', error);
            if (error.response && error.response.errors) {
                ValidationService.displayValidationErrors(error.response.errors);
            } else if (error.code === ErrorCodes.USERNAME_TAKEN) {
                ValidationService.displayValidationErrors([{
                    field: ErrorFields.USERNAME,
                    defaultMessage: error.message
                }]);
            } else if (error.code === ErrorCodes.EMAIL_TAKEN) {
                ValidationService.displayValidationErrors([{
                    field: ErrorFields.EMAIL,
                    defaultMessage: error.message
                }]);
            } else {
                showMessage(error.message || 'Ошибка при обновлении профиля', 'error');
            }
        }
    }

    setupLogout() {
        document.getElementById('logoutBtn').addEventListener('click', async () => {
            await TokenService.logout();
            window.location.href = '/pages/login.html';
        });
    }
} 