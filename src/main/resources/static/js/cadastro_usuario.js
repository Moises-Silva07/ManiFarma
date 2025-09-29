// Adiciona um "ouvinte" de evento que será acionado quando o formulário for enviado.
document.getElementById("form").addEventListener("submit", async (e) => {
    
    e.preventDefault();

    // 1. Coleta e limpa os valores dos campos.
    const nome = document.getElementById("nome").value.trim();
    const email = document.getElementById("email").value.trim();
    const cpf = document.getElementById("cpf").value.trim();
    const endereco = document.getElementById("endereco").value.trim();
    const telefone = document.getElementById("telefone").value.trim();
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

    // 4. Preparação para a API.
    const endpoint = "/api/auth/register";

    // --- MELHORIA AQUI: Construção explícita do corpo da requisição ---
    // Mapeia explicitamente as variáveis JS para os campos que o DTO Java espera.
    const requestBody = {
        nome: nome,
        email: email,
        senha: senha,
        cpf: cpf,
        endereco: endereco,
        telefone: telefone,
        client: true // Define que este é um cadastro de cliente.
    };
    // ----------------------------------------------------------------

    // 5. Chamada para a API usando o corpo explícito.
    const {ok, data} = await apiRequest(endpoint, "POST", requestBody);

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
