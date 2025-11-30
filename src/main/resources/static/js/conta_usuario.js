// Verifica se o usuário está logado com Token
validarToken(); 

// Verifica se o usuário está logado com ID
const userId = localStorage.getItem("userId"); 
if (!userId) {
    showModal({
            title: "Erro",
            message: "Usuário não identificado. Faça login novamente.",
            type: "danger",
        });
    window.location.href = "/html/login/login.html";
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// VALIDADORES E FORMATADORES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

function validarEmail(email) {
    const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return regex.test(email);
}

function validarCPF(cpf) {
    return /^\d{11}$/.test(cpf.replace(/\D/g, ""));
}

function validarCEP(cep) {
    return /^[0-9]{8}$/.test(cep.replace(/\D/g, ""));
}

function validarTelefone(tel) {
    const regex = /^\(\d{2}\)\s\d{4,5}-\d{4}$/;
    return regex.test(tel);
}

// CARREGAR DADOS DO CLIENTE (Funcionando)
document.addEventListener("DOMContentLoaded", async () => {
    const resposta = await apiRequest(`/api/users/${userId}`, "GET", null, true);
    if (resposta.ok) {
        const dados = resposta.data;
        document.getElementById("nome").value = dados.nome || "";
        document.getElementById("cpf").value = dados.cpf || "";
        document.getElementById("email").value = dados.email || "";
        document.getElementById("telefone").value = dados.telefone || "";
        document.getElementById("cep").value = dados.cep || "";
        document.getElementById("rua").value = dados.rua || "";
        document.getElementById("bairro").value = dados.bairro || "";
        document.getElementById("cidade").value = dados.cidade || "";
        document.getElementById("uf").value = dados.estado || "";
        
    } else {
        showModal({
            title: "Erro",
            message: "Erro ao carregar dados do usuário",
            type: "danger",
        });
        console.error(resposta.data);
    }
});

// ATUALIZAR DADOS (Corrigido com validações funcionando)
document.getElementById("form-dados").addEventListener("submit", async (e) => {
    e.preventDefault();

    // PEGAR OS VALORES AQUI  ✔ (ANTES DO BODY)
    const nome = document.getElementById("nome").value.trim();
    const cpf = document.getElementById("cpf").value.trim();
    const email = document.getElementById("email").value.trim();
    const telefone = document.getElementById("telefone").value.trim();
    const cep = document.getElementById("cep").value.trim();
    const rua = document.getElementById("rua").value.trim();
    const bairro = document.getElementById("bairro").value.trim();
    const cidade = document.getElementById("cidade").value.trim();
    const estado = document.getElementById("uf").value.trim();

    // ━━━━━ Validações ━━━━━
    if (!validarEmail(email)) {
        return showModal({
            title: "E-mail inválido",
            message: "Digite um e-mail válido.",
            type: "warning",
        });
    }

    if (!validarCPF(cpf)) {
        return showModal({
            title: "CPF inválido",
            message: "Digite somente números (11 dígitos).",
            type: "warning",
        });
    }

    if (!validarTelefone(telefone)) {
        return showModal({
            title: "Telefone inválido",
            message: "Use o formato (99) 99999-9999",
            type: "warning",
        });
    }

    if (!validarCEP(cep)) {
        return showModal({
            title: "CEP inválido",
            message: "Digite 8 números.",
            type: "warning",
        });
    }

    if (rua === "" || rua === "...") {
        return showModal({
            title: "CEP não validado",
            message: "Não foi possível validar o endereço pelo CEP informado.",
            type: "danger",
        });
    }

    // JSON FINAL PARA ENVIAR
    const body = {
        nome,
        cpf,
        email,
        telefone,
        cep,
        rua,
        bairro,
        cidade,
        estado
    };

    const resposta = await apiRequest(`/api/users/${userId}`, "PUT", body, true);

    if (resposta.ok) {
        return showModal({
            title: "Sucesso!",
            message: "Dados atualizados com sucesso!",
            type: "success"
        });
    } else {
        return showModal({
            title: "Erro",
            message: resposta.data?.message || "Erro ao atualizar dados.",
            type: "danger",
        });
    }
});

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ATUALIZAR SENHA (VALIDAÇÃO + RESPOSTAS PERSONALIZADAS)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
document.getElementById("form-senha").addEventListener("submit", async (e) => {
    e.preventDefault();

    const senhaAtual = document.getElementById("senhaAtual").value.trim();
    const novaSenha = document.getElementById("novaSenha").value.trim();
    const confirma = document.getElementById("confirmaSenha").value.trim();

    if (!senhaAtual) {
        return showModal({
            title: "Atenção",
            message: "Digite sua senha atual.",
            type: "warning"
        });
    }

    // Senha forte
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#%])[A-Za-z\d@$!%*?&#%]{8,}$/;
    if (!regex.test(novaSenha)) {
        return showModal({
            title: "Senha inválida",
            message: "A nova senha deve ter 8 caracteres, contendo letra maiúscula, minúscula, número e caractere especial.",
            type: "warning"
        });
    }

    if (novaSenha !== confirma) {
        return showModal({
            title: "Atenção",
            message: "As senhas não coincidem.",
            type: "warning",
        });
    }

    const body = { senhaAtual, novaSenha };
    const resposta = await apiRequest(`/api/users/${userId}/senha`, "PUT", body, true);

    if (resposta.ok) {
        return showModal({
            title: "Sucesso!",
            message: "Senha alterada com sucesso!",
            type: "success"
        });
    }

    // Se backend retornar senha atual incorreta
    if (resposta.status === 400 || resposta.status === 401) {
        return showModal({
            title: "Senha incorreta",
            message: "A senha atual não confere.",
            type: "danger"
        });
    }

    return showModal({
        title: "Erro",
        message: "Erro ao alterar senha.",
        type: "danger",
    });
});


// DESATIVAR CONTA
document.getElementById("btn-excluir-conta").addEventListener("click", async () => {
    const confirmar = await showModal({
        title: "Confirmação",
        message: "Tem certeza que deseja desativar sua conta?",
        type: "confirm"
    });

    if (!confirmar) return; // Usuário cancelou

    try {
        const resposta = await apiRequest(`/api/users/${userId}/toggle-activation`, "PATCH", null, true);

        if (resposta.ok) {
            showToast("Conta desativada com sucesso!", "success");

            // Espera 2 segundos e redireciona
            setTimeout(() => {
                localStorage.clear();
                window.location.href = "/html/index.html";
            }, 2000);
        } else {
            showModal({
                title: "Erro",
                message: "Não foi possível concluir a desativação da conta. Tente novamente.",
                type: "danger"
            });
            console.error(resposta.data);
        }
    } catch (error) {
        console.error("Erro ao desativar conta:", error);
        showModal({
            title: "Erro inesperado",
            message: "Ocorreu um problema ao tentar desativar a conta.",
            type: "danger"
        });
    }
});



// API VIACEP
function limpa_formulário_cep() {
            //Limpa valores do formulário de cep.
            document.getElementById('rua').value=("");
            document.getElementById('bairro').value=("");
            document.getElementById('cidade').value=("");
            document.getElementById('uf').value=("");
            
    }

    function meu_callback(conteudo) {
        if (!("erro" in conteudo)) {
            //Atualiza os campos com os valores.
            document.getElementById('rua').value=(conteudo.logradouro);
            document.getElementById('bairro').value=(conteudo.bairro);
            document.getElementById('cidade').value=(conteudo.localidade);
            document.getElementById('uf').value=(conteudo.uf);
            
        } //end if.
        else {
            //CEP não Encontrado.
            limpa_formulário_cep();
            showModal({
                title: "Atenção",
                message: "CEP não encontrado.",
                type: "warning"
            });
        }
    }

function pesquisacep(valor) {
    var cep = valor.replace(/\D/g, '');

    if (cep !== "") {
        var validacep = /^[0-9]{8}$/;

        if (validacep.test(cep)) {

            // Preenche com "..."
            document.getElementById('rua').value = "...";
            document.getElementById('bairro').value = "...";
            document.getElementById('cidade').value = "...";
            document.getElementById('uf').value = "...";

            // 🔥 REMOVE scripts anteriores para evitar duplicação
            const scriptsAntigos = document.querySelectorAll("script[data-viacep]");
            scriptsAntigos.forEach(s => s.remove());

            // 🔥 Cria o script ViaCEP
            var script = document.createElement('script');
            script.setAttribute("data-viacep", "true");
            script.src = 'https://viacep.com.br/ws/' + cep + '/json/?callback=meu_callback';

            // 🔥 Timeout para caso o servidor não responda
            const timeout = setTimeout(() => {
                script.remove(); // remove o script para evitar callback tardio
                limpa_formulário_cep();
                showModal({
                    title: "Erro no CEP",
                    message: "Falha ao consultar o ViaCEP. O servidor pode estar fora do ar.",
                    type: "danger"
                });
            }, 4000); // 4 segundos

            // 🔥 Se o script carregar COM sucesso, o callback vai ser executado
            script.onload = () => clearTimeout(timeout);

            // 🔥 Se ocorrer erro de carregamento (servidor offline)
            script.onerror = () => {
                clearTimeout(timeout);
                limpa_formulário_cep();
                showModal({
                    title: "Erro no CEP",
                    message: "Não foi possível conectar ao serviço ViaCEP.",
                    type: "danger"
                });
            };

            // Carrega o script
            document.body.appendChild(script);

        } else {
            limpa_formulário_cep();
            showModal({
                title: "Erro",
                message: "Formato de CEP inválido.",
                type: "danger"
            });
        }

    } else {
        limpa_formulário_cep();
    }
}