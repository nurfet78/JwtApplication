export function showMessage(text, type = 'info') {
    const messageEl = document.getElementById('message');
    if (messageEl) {
        messageEl.textContent = text;
        
        // Удаляем все предыдущие классы
        messageEl.className = 'alert';
        
        // Добавляем класс в зависимости от типа сообщения
        switch (type) {
            case 'success':
                messageEl.classList.add('alert-success');
                break;
            case 'error':
                messageEl.classList.add('alert-error');
                break;
            case 'danger':
                messageEl.classList.add('alert-danger');
                break;
            default:
                messageEl.classList.add('alert-info');
        }

        // Автоматически скрываем сообщение через 5 секунд
        setTimeout(() => {
            messageEl.textContent = '';
            messageEl.className = 'alert';
        }, 5000);
    }
} 