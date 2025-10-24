document.getElementById("cadastroForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    // Pegando os valores corretos do formulário
    const nome = document.getElementById("nomeCompleto").value;
    const email = document.getElementById("nomeAcesso").value;
    const senha = document.getElementById("senha").value;
    const confirmaSenha = document.getElementById("confirmaSenha").value;
    const role = document.getElementById("cargo").value;

    // Validação de senha
    if (senha !== confirmaSenha) {
        document.getElementById("mensagem").textContent = "As senhas não conferem!";
        return;
    }

    // Monta payload apenas com os campos necessários para Employee
    const payload = {
        nome,
        email,
        senha,
        isClient: false,
        role
    };

    console.log("%c>>> Payload preparado para envio:", "color: green; font-weight: bold;");
    console.log(payload);

    // Faz a requisição para o backend
    const { ok, data } = await apiRequest("/api/auth/register", "POST", payload);

    if (ok) {
        console.log("%c>>> Resposta de sucesso recebida do servidor:", "color: green; font-weight: bold;");
        console.log(data);

        document.getElementById("mensagem").textContent = "Cadastro realizado com sucesso!";
        document.getElementById("cadastroForm").reset();
    } else {
        console.error("%c>>> Erro no cadastro:", "color: red; font-weight: bold;");
        console.error(data);

        // Mostra a mensagem do backend se existir
        document.getElementById("mensagem").textContent =
            (data && data.mensagem) ? data.mensagem : "Cadastro inválido!";
    }
});