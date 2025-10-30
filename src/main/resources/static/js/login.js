document.getElementById("form").addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;   
    const senha = document.getElementById("senha").value;

    const endpoint = "/api/auth/login";

    // Adicionado parâmetro debug = true para ajudar a testar login
    const { ok, data } = await apiRequest(endpoint, "POST", { email, senha }, false, true);

    console.log("RESPONSE DATA (login):", data);
    console.table(Object.entries(data));

    if (ok) {
        // Salva token e ID do cliente (ou funcionário)
        localStorage.setItem("token", data.token);
        localStorage.setItem("clienteId", data.id);
        localStorage.setItem("userId", data.id);

        // Salva também o usuário completo (opcional, útil em páginas futuras)
        localStorage.setItem("user", JSON.stringify(data));

        document.getElementById("message").textContent = "Login realizado com sucesso!";

        
        setTimeout(() => {
            // Confirma se o backend realmente retorna "client" (true/false)
            // Se o campo for diferente (ex: "cliente" ou "isClient"), troque aqui
            if (data.client) {
                window.location.href = "/html/menu_usuario/menu_usuario.html";
            } else {
                window.location.href = "/html/menu_funcionario/menu_funcionario.html";
            }
        }, 1000);

    } else {
        // Mensagem de erro
        document.getElementById("message").textContent =
            data?.mensagem || "Credenciais inválidas ou erro de conexão.";
    }
});