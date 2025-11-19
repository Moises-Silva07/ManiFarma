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

// ATUALIZAR DADOS (Funcioando)
document.getElementById("form-dados").addEventListener("submit", async (e) => {
    e.preventDefault();

    const body = {
        nome: document.getElementById("nome").value,
        cpf: document.getElementById("cpf").value,
        email: document.getElementById("email").value,
        telefone: document.getElementById("telefone").value,
        cep: document.getElementById("cep").value,
        rua: document.getElementById("rua").value,
        bairro: document.getElementById("bairro").value,
        cidade: document.getElementById("cidade").value,
        estado: document.getElementById("uf").value,
        
    };

    const resposta = await apiRequest(`/api/users/${userId}`, "PUT", body, true, true);
    if (resposta.ok) {
        showModal({
            title: "Sucesso!",
            message: "Dados atualizados com sucesso!",
            type: "success"
        });
    } else {
        showModal({
            title: "Erro",
            message: "Erro ao atualizar dados.",
            type: "danger",
        });
        console.error(resposta.data);
    }
});

// ATUALIZAR SENHA (Funcionando)
document.getElementById("form-senha").addEventListener("submit", async (e) => {
    e.preventDefault();

    const senhaAtual = document.getElementById("senhaAtual").value;
    const novaSenha = document.getElementById("novaSenha").value;
    const confirma = document.getElementById("confirmaSenha").value;


    // Expressão regular para validar:
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#%])[A-Za-z\d@$!%*?&#%]{8,}$/;


    if (!regex.test(novaSenha)) {
        document.getElementById("message").textContent = "A senha deve ter no mínimo 8 caracteres, incluindo letra maiúscula (A-Z), minúscula(a-z), número(1-9) e caractere especial(@$!%*?&).";
        return;
    }

    if (novaSenha !== confirma) {
        showModal({
            title: "Atenção",
            message: "As senhas não coincidem.",
            type: "warning",
        });
        return;
    }

    const body = { senhaAtual, novaSenha };
    const resposta = await apiRequest(`/api/users/${userId}/senha`, "PUT", body, true);

    if (resposta.ok) {
        showModal({
            title: "Sucesso!",
            message: "Senha alterada com sucesso!",
            type: "success"
        });
    } else {
        showModal({
            title: "Erro",
            message: "Erro ao alterar senha.",
            type: "danger",
        });
        console.error(resposta.data);
    }
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

        //Nova variável "cep" somente com dígitos.
        var cep = valor.replace(/\D/g, '');

        //Verifica se campo cep possui valor informado.
        if (cep != "") {

            //Expressão regular para validar o CEP.
            var validacep = /^[0-9]{8}$/;

            //Valida o formato do CEP.
            if(validacep.test(cep)) {

                //Preenche os campos com "..." enquanto consulta webservice.
                document.getElementById('rua').value="...";
                document.getElementById('bairro').value="...";
                document.getElementById('cidade').value="...";
                document.getElementById('uf').value="...";
                

                //Cria um elemento javascript.
                var script = document.createElement('script');

                //Sincroniza com o callback.
                script.src = 'https://viacep.com.br/ws/'+ cep + '/json/?callback=meu_callback';

                //Insere script no documento e carrega o conteúdo.
                document.body.appendChild(script);

            } //end if.
            else {
                //cep é inválido.
                limpa_formulário_cep();
                showModal({
                    title: "Erro",
                    message: "Formato de CEP inválido.",
                    type: "danger"
                });
            }
        } //end if.
        else {
            //cep sem valor, limpa formulário.
            limpa_formulário_cep();
        }
    };