import { UserApi } from '../api/userApi.js';
import { TokenService } from '../services/tokenService.js';
import { showMessage } from '../utils/messageUtil.js';

export class ProfileController {
    constructor() {
        this.init();
    }

    async init() {
        if (!TokenService.getAccessToken()) {
            window.location.href = '/pages/login.html';
            return;
        }
        await this.loadProfile();
        this.setupLogout();
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

    async loadProfile() {
        try {
            const response = await UserApi.getProfile();
            if (response.data) {
                const data = response.data;
                document.getElementById('firstName').textContent = data.firstName;
                document.getElementById('lastName').textContent = data.lastName;
                document.getElementById('email').textContent = data.email;
                document.getElementById('username').textContent = data.username;
                document.getElementById('lastLogin').textContent = 
                    new Date(data.lastLogin).toLocaleString();
            }
        } catch (error) {
            showMessage('Ошибка загрузки профиля', 'error');
            console.error('Error loading profile:', error);
        }
    }
} 