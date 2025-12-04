// URL base da API
const apiUrl = "http://localhost:8080";


async function apiRequest(endpoint, method, body = null, auth = false, debug = true) {
    
    // --- Início da Depuração ---
    if (debug) console.clear();
    if (debug) {
        console.log("%c--- INICIANDO NOVA REQUISIÇÃO API ---", "font-weight: bold;");
        console.log(`1. Parâmetros recebidos: endpoint='${endpoint}', method='${method}'`);
        console.log("2. Corpo recebido (antes de converter para JSON):", body);
    }
    // --- Fim da Depuração ---

    const headers = {};

    // Só coloca Content-Type se NÃO for FormData
    if (!(body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }

    if (auth) {
        const token = localStorage.getItem("token");
        if (token) headers["Authorization"] = `Bearer ${token}`;
    }

    const options = { method, headers };

    // Se for JSON → converte
    // Se for FormData → usa diretamente
    if (body) {
        if (body instanceof FormData) {
            options.body = body;
        } else {
            options.body = JSON.stringify(body);
        }
    }
    
    try {
        const fullUrl = `${apiUrl}${endpoint}`; // Junta a URL base com o endpoint (exemplo: http://localhost:8080/api/users).

        // --- Depuração Adicional ---
        if (debug) {
            console.log(`3. URL completa que será chamada: ${fullUrl}`);
            console.log("4. Objeto 'options' final enviado para o fetch:", options);
        }
        // --- Fim da Depuração Adicional ---

        const resposta = await fetch(fullUrl, options); // Faz a requisição HTTP e espera a resposta do servidor.

        
        // --- Depuração da Resposta ---
        if (debug) {
            console.log(`5. Resposta recebida do servidor: ${resposta.status} ${resposta.statusText}`); // Mostra o código e o texto da resposta (ex: 200 OK ou 404 Not Found).
        }
        // --- Fim da Depuração da Resposta ---

        if (resposta.status === 204) { // 204 = sem conteúdo
            return { ok: resposta.ok, data: null }; // A função devolve data: null para evitar erro ao tentar converter JSON vazio.
        }


        let data = null;
        try {
            // Tenta ler como JSON
            const text = await resposta.text(); 
            data = text ? JSON.parse(text) : null;
        } catch {
            data = null; // fallback seguro
        }

        return { 
            ok: resposta.ok,
            status: resposta.status,
            data 
        }; // ok: indica se a requisição deu certo (true/false); // data: dados retornados pela API.


    } catch (erro) { // Se algo falhar (como o servidor estar offline), o catch captura o erro e devolve uma resposta padronizada avisando o usuário.
        
        console.error("%c!!! ERRO CRÍTICO CAPTURADO PELO CATCH !!!", "color: red; font-weight: bold; font-size: 14px;");
        console.error("O erro que causou a falha na conexão foi:", erro);

        return {
            ok: false,
            status: 0,
            data: {
                mensagem: "Erro na conexão com o servidor.",
                erro: erro.message
            }
        };
    }
}

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
            alert("CEP não encontrado.");
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
                alert("Formato de CEP inválido.");
            }
        } //end if.
        else {
            //cep sem valor, limpa formulário.
            limpa_formulário_cep();
        }
    };