// Adiciona um "ouvinte" de evento que será acionado quando o formulário for enviado.
document.getElementById("form").addEventListener("submit", async (e) => {
    
    e.preventDefault();

    // 1. Coleta e limpa os valores dos campos.
    const senha = document.getElementById("senha").value;
    const confirmaSenha = document.getElementById("confirmaSenha").value;

    // 2. Depuração para validação da senha.
    console.log("Valor do campo 'senha':", `"${senha}"`);
    console.log("Valor do campo 'confirmaSenha':", `"${confirmaSenha}"`);

    // 3. Validação das senhas.
    if (senha !== confirmaSenha) {
        document.getElementById("message").textContent = "As senhas não conferem!";
        return; 
    }

    // --- MELHORIA AQUI: Construção explícita do corpo da requisição ---
    // Mapeia explicitamente as variáveis JS para os campos que o DTO Java espera.
    const requestBody = {
        nome: document.getElementById("nome").value.trim(),
        email: document.getElementById("email").value.trim(),
        senha: senha,
        cpf: document.getElementById("cpf").value.trim(),
        telefone: document.getElementById("telefone").value.trim(),
        cep: document.getElementById("cep").value.trim(),
        client: true
    };
    // ----------------------------------------------------------------

    // 5. Chamada para a API usando o corpo explícito.
    const { ok, data } = await apiRequest("/api/auth/register", "POST", requestBody);

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
