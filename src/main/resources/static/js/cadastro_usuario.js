// Adiciona um "ouvinte" de evento que será acionado quando o formulário for enviado.
document.getElementById("form").addEventListener("submit", async (e) => {
    
    e.preventDefault();

    // 1. Coleta e limpa os valores dos campos.
    const senha = document.getElementById("senha").value;
    const confirmaSenha = document.getElementById("confirmaSenha").value;

    // 2. Depuração para validação da senha.
    console.log("Valor do campo 'senha':", `"${senha}"`);
    console.log("Valor do campo 'confirmaSenha':", `"${confirmaSenha}"`);

    // Expressão regular para validar:
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#%])[A-Za-z\d@$!%*?&#%]{8,}$/;


    if (!regex.test(senha)) {
        document.getElementById("message").textContent = "A senha deve ter no mínimo 8 caracteres, incluindo letra maiúscula (A-Z), minúscula(a-z), número(1-9) e caractere especial(@$!%*?&).";
        return;
    }

    // 3. Validação das senhas.
    if (senha !== confirmaSenha) {
        document.getElementById("message").textContent = "As senhas não conferem!";
        return; 
    }

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
    
    const submitButton = e.target.querySelector("button[type='submit']");
    submitButton.disabled = true;

    // 5. Chamada para a API usando o corpo explícito.
    const { ok, data } = await apiRequest("/api/auth/register", "POST", requestBody);
    submitButton.disabled = false;

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
