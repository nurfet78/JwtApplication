import { UserApi } from '../api/userApi.js';
import { TokenService } from '../services/tokenService.js';
import { showMessage } from '../utils/messageUtil.js';

export class DashboardController {
    constructor() {
        this.init();
    }

    async init() {
        if (!TokenService.getAccessToken()) {
            window.location.href = '/pages/login.html';
            return;
        }

        this.setupLogout();
        await this.loadAdminProfile();
    }

    setupLogout() {
        document.getElementById('logoutBtn').addEventListener('click', async () => {
            try {
                await TokenService.logout();
                setTimeout(() => {
                    window.location.href = '/pages/login.html';
                }, 1000);
            } catch (error) {
                console.error('Error during logout:', error);
            }
        });
    }

    async loadAdminProfile() {
        try {
            const response = await UserApi.getProfile();
            if (response.data) {
                const data = response.data;
                document.getElementById('firstName').textContent = data.firstName;
                document.getElementById('lastName').textContent = data.lastName;
                document.getElementById('email').textContent = data.email;
                document.getElementById('roles').textContent = data.roles;
            }
        } catch (error) {
            showMessage('Ошибка загрузки профиля администратора', 'error');
            console.error('Error loading admin profile:', error);
        }
    }
} 