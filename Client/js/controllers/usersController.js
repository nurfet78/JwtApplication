import { AdminApi } from '../api/adminApi.js';
import { RoleApi } from '../api/roleApi.js';
import { TokenService } from '../services/tokenService.js';
import { showMessage } from '../utils/messageUtil.js';

export class UsersController {
    constructor() {
        this.init();
    }

    async init() {
        if (!TokenService.getAccessToken()) {
            window.location.href = '/pages/login.html';
            return;
        }

        // Инициализируем компоненты
        this.setupLogout();
        this.setupModal();
        this.modal = new bootstrap.Modal(document.getElementById('editModal'));
        
        // Загружаем данные - здесь сработает 403 ошибка для пользователя без прав админа
        await this.loadRoles();
        await this.loadUsers();
    }

    setupLogout() {
        document.getElementById('logoutBtn').addEventListener('click', () => {
            TokenService.removeTokens();
            window.location.href = '/pages/login.html';
        });
    }

    setupModal() {
        const form = document.getElementById('editUserForm');
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.handleEditSubmit(form);
        });
    }

    async loadUsers() {
        try {
            const users = await AdminApi.getAllUsers();
            const tbody = document.getElementById('usersTableBody');
            const template = document.getElementById('userRowTemplate');
            
            tbody.innerHTML = '';

            users.forEach(user => {
                const clone = template.content.cloneNode(true);
                
                clone.querySelector('.user-id').textContent = user.id;
                clone.querySelector('.user-firstName').textContent = user.firstName;
                clone.querySelector('.user-lastName').textContent = user.lastName;
                clone.querySelector('.user-email').textContent = user.email;
                
                const roles = Array.isArray(user.roles) ? user.roles : [user.roles];
                const formattedRoles = roles.map(role => 
                    typeof role === 'string' ? role.replace('ROLE_', '') : 
                    role.authority ? role.authority.replace('ROLE_', '') : ''
                ).join(', ');
                clone.querySelector('.user-roles').textContent = formattedRoles;

                const editBtn = clone.querySelector('.edit-user');
                const deleteBtn = clone.querySelector('.delete-user');
                const revokeBtn = clone.querySelector('.revoke-token');

                editBtn.addEventListener('click', () => this.editUser(user.id));
                deleteBtn.addEventListener('click', () => this.deleteUser(user.id));
                revokeBtn.addEventListener('click', async () => {
                    try {
                        if (confirm('Вы уверены, что хотите отозвать все токены этого пользователя?')) {
                            const message = await AdminApi.revokeRefreshToken(user.id);
                            showMessage(message, 'success');

                            // Проверяем, отозвали ли мы свои собственные токены
                            const currentUserId = user.id; // ID текущего пользователя в строке таблицы
                            const adminId = TokenService.getUserIdFromToken(TokenService.getAccessToken()); // Получаем ID админа из токена

                            if (currentUserId === adminId) {
                                // Если отозвали свои токены, выходим из системы
                                showMessage('Ваша сессия завершена', 'info');
                                setTimeout(() => {
                                    TokenService.removeTokens();
                                    window.location.href = '/pages/login.html';
                                }, 1500);
                            }
                        }
                    } catch (error) {
                        showMessage(error.message, 'error');
                    }
                });

                tbody.appendChild(clone);
            });
        } catch (error) {
            showMessage('Ошибка загрузки пользователей', 'error');
            console.error('Error loading users:', error);
        }
    }

    async editUser(id) {
        try {
            const user = await AdminApi.getUser(id);
            
            document.getElementById('userId').value = user.id;
            document.getElementById('editFirstName').value = user.firstName;
            document.getElementById('editLastName').value = user.lastName;
            document.getElementById('editEmail').value = user.email;
            document.getElementById('editUsername').value = user.username;
            document.getElementById('editPassword').value = '';

            const rolesSelect = document.getElementById('editRoles');
            const userRoles = Array.isArray(user.roles) ? user.roles : [user.roles];
            
            Array.from(rolesSelect.options).forEach(option => {
                option.selected = userRoles.includes(option.value);
            });

            this.modal.show();
        } catch (error) {
            showMessage('Ошибка при загрузке данных пользователя', 'error');
            console.error('Error editing user:', error);
        }
    }

    displayValidationErrors(errors) {
        this.clearValidationErrors();
        errors.forEach(error => {
            const field = error.field;
            const message = error.defaultMessage;
            
            const input = document.getElementById('edit' + field.charAt(0).toUpperCase() + field.slice(1));
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

    async handleEditSubmit(form) {
        try {
            this.clearValidationErrors();

            const rolesSelect = document.getElementById('editRoles');
            const selectedRoles = Array.from(rolesSelect.selectedOptions).map(option => option.value);

            const userData = {
                id: document.getElementById('userId').value,
                firstName: document.getElementById('editFirstName').value,
                lastName: document.getElementById('editLastName').value,
                email: document.getElementById('editEmail').value,
                username: document.getElementById('editUsername').value,
                password: document.getElementById('editPassword').value,
                roles: selectedRoles
            };

            if (!userData.password) {
                delete userData.password;
            }

            const response = await AdminApi.updateUser(userData);
            
            if (response.errors) {
                this.displayValidationErrors(response.errors);
                return;
            }

            showMessage(response.message || 'Пользователь успешно обновлен', 'success');
            this.modal.hide();
            await this.loadUsers();
        } catch (error) {
            if (error.response && error.response.errors) {
                this.displayValidationErrors(error.response.errors);
            } else {
                showMessage(error.message || 'Ошибка при обновлении пользователя', 'error');
                console.error('Error updating user:', error);
            }
        }
    }

    async deleteUser(id) {
        if (confirm('Вы уверены, что хотите удалить этого пользователя?')) {
            try {
                await AdminApi.deleteUser(id);
                showMessage('Пользователь успешно удален', 'success');
                await this.loadUsers();
            } catch (error) {
                showMessage('Ошибка при удалении пользователя', 'error');
                console.error('Error deleting user:', error);
            }
        }
    }

    async loadRoles() {
        try {
            const roles = await RoleApi.getAllRoles();
            console.log('Loaded roles:', roles);

            const rolesSelect = document.getElementById('editRoles');
            rolesSelect.innerHTML = '';
            
            roles.forEach(role => {
                const option = document.createElement('option');
                option.value = role.authority;
                option.textContent = role.authority.replace('ROLE_', '');
                rolesSelect.appendChild(option);
            });
        } catch (error) {
            showMessage('Ошибка загрузки ролей', 'error');
            console.error('Error loading roles:', error);
        }
    }
} 