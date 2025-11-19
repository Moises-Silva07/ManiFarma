// Função genérica para verificar se o token existe
function validarToken() {
    const token = localStorage.getItem("token");

    if (!token) {
        // Se não existir, limpa e manda o usuário pro login
        localStorage.clear();
        window.location.href = "/html/login/login.html";
    }
}