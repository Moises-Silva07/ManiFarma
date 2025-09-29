// Garante que o código só rode depois que o HTML inteiro for carregado.
document.addEventListener('DOMContentLoaded', () => {

    // Encontra o botão pelo ID que definimos no HTML.
    const botaoExcluir = document.getElementById('btn-excluir-conta');

    // Se o botão for encontrado, adiciona o evento de clique.
    if (botaoExcluir) {
        botaoExcluir.addEventListener('click', async () => {

            // Pede a confirmação do usuário.
            if (!confirm("Tem certeza que deseja excluir sua conta? Esta ação é irreversível.")) {
                return; // Se o usuário clicar em "Cancelar", a função para aqui.
            }

            // Prepara os detalhes para a chamada da API.
            const endpoint = "/api/users/me";
            const method = "DELETE";
            const requiresAuth = true; // Essencial para enviar o token.

            // Chama a função genérica da API.
            const { ok, data } = await apiRequest(endpoint, method, null, requiresAuth);

            // Trata a resposta do backend.
            if (ok) {
                alert("Conta excluída com sucesso!");
                localStorage.clear(); // Limpa o token do navegador.
                window.location.href = "/html/login/login.html"; // Redireciona para o login.
            } else {
                // Mostra a mensagem de erro vinda do backend ou uma mensagem padrão.
                const errorMessage = data?.mensagem || "Não foi possível excluir a conta.";
                alert(errorMessage);
            }
        });
    } else {
        // Este console.error é um último recurso de segurança para depuração.
        console.error("ERRO CRÍTICO: O botão com id 'btn-excluir-conta' não foi encontrado. Verifique o HTML.");
    }
});
