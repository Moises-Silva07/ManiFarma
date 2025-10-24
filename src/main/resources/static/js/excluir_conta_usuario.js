
document.addEventListener('DOMContentLoaded', () => {


    const botaoExcluir = document.getElementById('btn-excluir-conta');


    if (botaoExcluir) {
        botaoExcluir.addEventListener('click', async () => {


            if (!confirm("Tem certeza que deseja excluir sua conta? Esta ação é irreversível.")) {
                return;
            }


            const endpoint = "/api/users/me";
            const method = "DELETE";
            const requiresAuth = true;


            const { ok, data } = await apiRequest(endpoint, method, null, requiresAuth);


            if (ok) {
                alert("Conta excluída com sucesso!");
                localStorage.clear();
                window.location.href = "/html/login/login.html";
            } else {

                const errorMessage = data?.mensagem || "Não foi possível excluir a conta.";
                alert(errorMessage);
            }
        });
    } else {

        console.error("ERRO CRÍTICO: O botão com id 'btn-excluir-conta' não foi encontrado. Verifique o HTML.");
    }
});
