export class ValidationService {
    static displayValidationErrors(errors, prefix = '') {
        console.log('Displaying validation errors:', errors);
        console.log('Using prefix:', prefix);

        this.clearValidationErrors();
        errors.forEach(error => {
            const field = error.field;
            const message = error.defaultMessage;

            // Добавляем префикс к ID поля
            const inputId = prefix ? prefix + field.charAt(0).toUpperCase() + field.slice(1) : field;
            const input = document.getElementById(inputId);

            console.log('Looking for input with ID:', inputId);
            console.log('Found input:', input);

            if (input) {
                input.classList.add('is-invalid');

                // Проверяем, существует ли уже feedback div
                let feedbackDiv = input.nextElementSibling;
                if (!feedbackDiv || !feedbackDiv.classList.contains('invalid-feedback')) {
                    feedbackDiv = document.createElement('div');
                    feedbackDiv.className = 'invalid-feedback';
                    input.parentNode.appendChild(feedbackDiv);
                }

                feedbackDiv.textContent = message;
            } else {
                console.warn(`Input element not found for field: ${inputId}`);
            }
        });
    }

    static clearValidationErrors() {
        const feedbacks = document.querySelectorAll('.invalid-feedback');
        const invalids = document.querySelectorAll('.is-invalid');

        console.log('Clearing validation errors');
        console.log('Found feedback elements:', feedbacks.length);
        console.log('Found invalid elements:', invalids.length);

        feedbacks.forEach(el => el.remove());
        invalids.forEach(el => el.classList.remove('is-invalid'));
    }

    static clearFormValidationErrors(form) {
        const inputs = form.querySelectorAll('.is-invalid');
        console.log('Clearing form validation errors:', inputs.length);
        inputs.forEach(input => input.classList.remove('is-invalid'));
    }
}