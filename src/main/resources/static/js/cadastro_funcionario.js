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

    // Faz a requisição para o backend
    const { ok, data } = await apiRequest(" ", "POST", payload);

    if (ok) {
        document.getElementById("mensagem").textContent = "Cadastro realizado com sucesso!";
        // Opcional: limpar formulário
        document.getElementById("cadastroForm").reset();
    } else {
        document.getElementById("mensagem").textContent = data.mensagem || "Cadastro inválido!";
    }
});