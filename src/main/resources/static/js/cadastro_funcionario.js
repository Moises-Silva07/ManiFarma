document.getElementById("form").addEventListener("submit", async (e) => {
    e.preventDefault();

    // Pegando os valores corretos do formulário
    const nome = document.getElementById("nomeCompleto").value;
    const email = document.getElementById("nomeAcesso").value;
    const senha = document.getElementById("senha").value;
    const confirmaSenha = document.getElementById("confirmaSenha").value;
    const role = document.getElementById("cargo").value;

    // Validação de senha
    if (senha !== confirmaSenha) {
        document.getElementById("message").textContent = "As senhas não conferem!";
        return;
    }

    // 4. Preparação para a API.
    const endpoint = "/api/auth/register";

    // Monta payload apenas com os campos necessários para Employee
    const payload = {
        nome,
        email,
        senha,
        client: false,
        role
    };

    // Faz a requisição para o backend
    const { ok, data } = await apiRequest(endpoint, "POST", payload);

    // 6. Tratamento da Resposta.
    if (ok) {
        document.getElementById("message").textContent = "Cadastro realizado com sucesso! Redirecionando para o login...";
        setTimeout(() => { 
            window.location.href = "/html/login/login.html"; 
        }, 2000);
    } else {
        document.getElementById("message").textContent = data?.mensagem || "Cadastro inválido! Verifique os dados.";
    }
});
