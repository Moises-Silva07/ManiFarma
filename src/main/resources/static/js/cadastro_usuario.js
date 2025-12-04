// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// VALIDADORES E FORMATADORES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ━ ALTERADO ━  Formatar telefone automaticamente
document.getElementById("telefone").addEventListener("input", (e) => {
    let v = e.target.value.replace(/\D/g, "");

    if (v.length > 11) v = v.slice(0, 11);

    if (v.length <= 10) {
        e.target.value = v.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3");
    } else {
        e.target.value = v.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
    }
});

// ━ ALTERADO ━ Validação de e-mail real
function validarEmail(email) {
    const regex =
        /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return regex.test(email);
}

// ━ ALTERADO ━ Validação adicional de CPF (somente formato)
function validarCPF(cpf) {
    return /^\d{11}$/.test(cpf.replace(/\D/g, ""));
}

// ━ ALTERADO ━ Validar senha forte
function validarSenha(senha) {
    const regex =
        /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#%])[A-Za-z\d@$!%*?&#%]{8,}$/;
    return regex.test(senha);
}

// ━ ALTERADO ━ Validação básica de CEP
function validarCEP(cep) {
    return /^[0-9]{8}$/.test(cep.replace(/\D/g, ""));
}


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ENVIO DO FORMULÁRIO
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
document.getElementById("form").addEventListener("submit", async (e) => {
    e.preventDefault();

    // 🚨 NOVO — Verificar se usuário aceitou os termos
    const msg = document.getElementById("message");
    
    const aceitou = document.getElementById("aceitarTermos").checked;
    if (!aceitou) {
        msg.textContent = "Você precisa aceitar a Política de Privacidade para criar sua conta.";
        return;
    }

    const nome = document.getElementById("nome").value.trim();
    const email = document.getElementById("email").value.trim();
    const cpf = document.getElementById("cpf").value.trim();
    const cep = document.getElementById("cep").value.trim();
    const telefone = document.getElementById("telefone").value.trim();
    const senha = document.getElementById("senha").value.trim();
    const confirmaSenha = document.getElementById("confirmaSenha").value.trim();

    
    msg.textContent = "";

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. Validação de E-mail
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    if (!validarEmail(email)) {
        msg.textContent = "E-mail inválido.";
        return;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. Validação CPF
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    if (!validarCPF(cpf)) {
        msg.textContent = "CPF inválido. Digite somente números (11 dígitos).";
        return;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. Validação CEP
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    if (!validarCEP(cep)) {
        msg.textContent = "CEP inválido. Digite 8 números.";
        return;
    }

    // Impede envio se o ViaCEP retornou vazio ou "..."
    if (
        document.getElementById("rua").value === "" ||
        document.getElementById("rua").value === "..."
    ) {
        msg.textContent = "CEP inválido. Não foi possível validar o endereço.";
        return;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4. Validação Telefone (formato + preenchimento)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    const padraoTelefone = /^\(\d{2}\)\s\d{4,5}-\d{4}$/;
    if (!padraoTelefone.test(telefone)) {
        msg.textContent = "Telefone inválido. Siga o formato (99) 99999-9999";
        return;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 5. Validação Senha
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    if (!validarSenha(senha)) {
        msg.textContent =
            "A senha deve ter no mínimo 8 caracteres, incluindo letra maiúscula, minúscula, número e caractere especial.";
        return;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 6. Confirmar Senha
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    if (senha !== confirmaSenha) {
        msg.textContent = "As senhas não conferem.";
        return;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // JSON final
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    const requestBody = {
        nome,
        email,
        senha,
        cpf,
        telefone,
        cep,
        rua: document.getElementById("rua").value,
        bairro: document.getElementById("bairro").value,
        cidade: document.getElementById("cidade").value,
        estado: document.getElementById("uf").value,
        client: true,
    };

    const submitButton = e.target.querySelector("button[type='submit']");
    submitButton.disabled = true;

    const { ok, data } = await apiRequest(
        "/api/auth/register",
        "POST",
        requestBody
    );

    submitButton.disabled = false;

    if (ok) {
        msg.textContent = "Cadastro realizado com sucesso! Redirecionando...";
        setTimeout(() => {
            window.location.href = "/html/login/login.html";
        }, 2000);
    } else {
        msg.textContent = data?.mensagem || "Erro ao cadastrar. Verifique os dados.";
    }
});


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// VIA CEP — Fluxo Novo e Corrigido
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

function limpa_formulario_cep() {
    document.getElementById("rua").value = "";
    document.getElementById("bairro").value = "";
    document.getElementById("cidade").value = "";
    document.getElementById("uf").value = "";
}

async function pesquisarCEP(valor) {
    const cep = valor.replace(/\D/g, "");

    // 1 — Verifica se tem algo
    if (cep === "") {
        limpa_formulario_cep();
        return;
    }

    // 2 — Verifica formato
    if (!/^[0-9]{8}$/.test(cep)) {
        limpa_formulario_cep();
        showModal({
            title: "Erro",
            message: "Formato de CEP inválido. Digite 8 números.",
            type: "danger"
        });
        return;
    }

    // Preenche com "..." enquanto busca
    document.getElementById("rua").value = "...";
    document.getElementById("bairro").value = "...";
    document.getElementById("cidade").value = "...";
    document.getElementById("uf").value = "...";

    // --- TIMEOUT MANUAL (3 segundos) ---
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 3000);

    try {
        const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`, {
            signal: controller.signal
        });

        clearTimeout(timeout);

        // Servidor respondeu, mas com erro (400, 500, etc)
        if (!response.ok) {
            limpa_formulario_cep();
            showModal({
                title: "Erro no servidor",
                message: "O serviço de CEP está indisponível no momento.",
                type: "danger"
            });
            return;
        }

        const data = await response.json();

        // CEP não encontrado
        if (data.erro) {
            limpa_formulario_cep();
            showModal({
                title: "CEP não encontrado",
                message: "Verifique se digitou corretamente.",
                type: "warning"
            });
            return;
        }

        // Caso raro — retorno sem logradouro
        if (!data.logradouro) {
            limpa_formulario_cep();
            showModal({
                title: "Endereço incompleto",
                message: "Não foi possível obter informações desse CEP.",
                type: "warning"
            });
            return;
        }

        // Preenche campos
        document.getElementById("rua").value = data.logradouro;
        document.getElementById("bairro").value = data.bairro;
        document.getElementById("cidade").value = data.localidade;
        document.getElementById("uf").value = data.uf;

    } catch (error) {
        clearTimeout(timeout);

        // Timeout do AbortController
        if (error.name === "AbortError") {
            limpa_formulario_cep();
            showModal({
                title: "Tempo excedido",
                message: "O serviço de CEP demorou demais para responder.",
                type: "danger"
            });
            return;
        }

        // Falha de rede: sem internet, DNS, CORS bloqueado, ViaCEP fora do ar
        limpa_formulario_cep();
        showModal({
            title: "Falha de conexão",
            message: "Não foi possível consultar o CEP agora. Tente novamente mais tarde.",
            type: "danger"
        });
    }
}

// ABRIR MODAL
document.getElementById("abrirPolitica").addEventListener("click", (e) => {
    e.preventDefault();
    document.getElementById("modal-politica").classList.remove("hidden");
});

// FECHAR MODAL
document.getElementById("fecharPolitica").addEventListener("click", () => {
    document.getElementById("modal-politica").classList.add("hidden");
});

