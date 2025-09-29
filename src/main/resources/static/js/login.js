// Arquivo: /js/login.js

document.getElementById("form").addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;   
    const senha = document.getElementById("senha").value;

    // ======================= CORREÇÃO AQUI =======================
    // Passe APENAS o caminho do endpoint, começando com a barra.
    const endpoint = "/api/auth/login";
    // =============================================================

    const { ok, data } = await apiRequest(endpoint, "POST", { email, senha });

    if (ok) {
        localStorage.setItem("token", data.token);
        localStorage.setItem("clienteId", data.id);

        document.getElementById("message").textContent = "Login realizado com sucesso!";

        setTimeout(() => {
            if (data.client) {
                window.location.href = "/html/menu_usuario/menu_usuario.html";
            } else {
                window.location.href = "/html/menu_funcionario/menu_funcionario.html";
            }
        }, 1000);

    } else {
        document.getElementById("message").textContent = data?.mensagem || "Credenciais inválidas.";
    }
});
