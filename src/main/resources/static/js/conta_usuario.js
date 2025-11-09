// CONFIGURAÇÃO DE VERIFICAÇÃO DE LOGIN (Funcioando)
const userId = localStorage.getItem("userId"); 
if (!userId) {
    showAlert("Usuário não identificado. Faça login novamente.", "danger");
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
        showAlert("Erro ao carregar dados do usuário", "danger");
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
        showToast("Dados atualizados com sucesso!", "success");
    } else {
        showAlert("Erro ao atualizar dados.", "danger");
        console.error(resposta.data);
    }
});

// ATUALIZAR SENHA (Funcionando)
document.getElementById("form-senha").addEventListener("submit", async (e) => {
    e.preventDefault();

    const senhaAtual = document.getElementById("senhaAtual").value;
    const novaSenha = document.getElementById("novaSenha").value;
    const confirma = document.getElementById("confirmaSenha").value;

    if (novaSenha !== confirma) {
        showAlert("As senhas não coincidem.", "warning");
        return;
    }

    const body = { senhaAtual, novaSenha };
    const resposta = await apiRequest(`/api/users/${userId}/senha`, "PUT", body, true);

    if (resposta.ok) {
        showToast("Senha alterada com sucesso!", "success");
    } else {
        showAlert("Erro ao alterar senha.");
        console.error(resposta.data);
    }
});


// DESATIVAR CONTA (Funcionando)
// Seleciona elementos
const modalElement = document.getElementById("modalConfirmarExclusao");
const confirmarBtn = document.getElementById("confirmar-exclusao");
const modalBootstrap = new bootstrap.Modal(modalElement);

document.getElementById("btn-excluir-conta").addEventListener("click", () => {
    // Apenas abre a modal, sem executar nada ainda
    modalBootstrap.show();
});

confirmarBtn.addEventListener("click", async () => {
    try {
        // Fecha a modal
        modalBootstrap.hide();

        // Faz a requisição
        const resposta = await apiRequest(`/api/users/${userId}/toggle-activation`, "PATCH", null, true);

        if (resposta.ok) {
            // Alerta de sucesso com outra modal Bootstrap
            const successModal = new bootstrap.Modal(document.getElementById("modalSucesso"));
            successModal.show();

            // Espera 2 segundos e redireciona
            setTimeout(() => {
                localStorage.clear();
                window.location.href = "/html/index.html";
            }, 2000);
        } else {
            showAlert("Não foi possível concluir a desativação da conta. Tente novamente.");
            console.error(resposta.data);
        }
    } catch (error) {
        console.error("Erro ao desativar conta:", error);
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
            showAlert("CEP não encontrado.", "warning");
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
                showAlert("Formato de CEP inválido.", "danger");
            }
        } //end if.
        else {
            //cep sem valor, limpa formulário.
            limpa_formulário_cep();
        }
    };