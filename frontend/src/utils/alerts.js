export function showAlert(message, type = 'info') {
    const alertBox = document.createElement('div');
    alertBox.className = `alert alert-${type}`;
    alertBox.textContent = message;
    document.body.appendChild(alertBox);
    setTimeout(() => {
        alertBox.classList.add('fade');
        setTimeout(() => {
            if (alertBox.parentNode) alertBox.parentNode.removeChild(alertBox);
        }, 300);
    }, 3000);
}

