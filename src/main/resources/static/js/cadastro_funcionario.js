document.getElementById("form").addEventListener("submit", async (e) => {
    e.preventDefault();

    const nome = document.getElementById("nomeCompleto").value.trim();
    const email = document.getElementById("nomeAcesso").value.trim();
    const senha = document.getElementById("senha").value.trim();
    const confirmaSenha = document.getElementById("confirmaSenha").value.trim();
    const role = document.getElementById("cargo").value.trim();

    const msg = document.getElementById("message");

    // --- Validações básicas -----------------------------

    if (!nome || nome.length < 3) {
        msg.textContent = "O nome deve ter pelo menos 3 caracteres.";
        return;
    }

    if (!email || !email.includes("@") || !email.includes(".")) {
        msg.textContent = "Digite um e-mail válido.";
        return;
    }

    if (!role) {
        msg.textContent = "Selecione um cargo.";
        return;
    }

    if (senha.length < 6) {
        msg.textContent = "A senha deve ter no mínimo 6 caracteres.";
        return;
    }

    if (senha !== confirmaSenha) {
        msg.textContent = "As senhas não conferem!";
        return;
    }

    //-----------------------------------------------------

    const endpoint = "/api/auth/register";

    const payload = {
        nome,
        email,
        senha,
        client: false,  // indicando funcionário
        role             // ADMIN / FARMACEUTICO / ATENDENTE
    };

    const { ok, data } = await apiRequest(endpoint, "POST", payload);

    if (ok) {
        msg.textContent = "Cadastro realizado com sucesso! Redirecionando para o login...";
        setTimeout(() => { 
            window.location.href = "/html/login/login.html"; 
        }, 2000);
    } else {
        msg.textContent = data?.mensagem || "Cadastro inválido! Verifique os dados.";
    }
});
