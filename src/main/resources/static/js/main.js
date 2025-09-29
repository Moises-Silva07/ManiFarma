// URL base do backend
const apiUrl = "http://localhost:8080";

// Função utilitária com depuração detalhada
async function apiRequest(endpoint, method, body = null, auth = false ) {
    
    // --- Início da Depuração ---
    console.clear(); // Limpa o console para focar nesta requisição
    console.log("%c--- INICIANDO NOVA REQUISIÇÃO API ---", "color: blue; font-weight: bold;");
    console.log(`1. Parâmetros recebidos: endpoint='${endpoint}', method='${method}'`);
    console.log("2. Corpo recebido (antes de converter para JSON):", body);
    // --- Fim da Depuração ---

    const headers = { "Content-Type": "application/json" };
    
    if (auth) {
        const token = localStorage.getItem("token");
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }
    }

    const options = {
        method: method,
        headers: headers,
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const fullUrl = `${apiUrl}${endpoint}`;

        // --- Depuração Adicional ---
        console.log(`3. URL completa que será chamada: ${fullUrl}`);
        console.log("4. Objeto 'options' final enviado para o fetch:", options);
        // --- Fim da Depuração Adicional ---

        const resposta = await fetch(fullUrl, options);

        // --- Depuração da Resposta ---
        console.log(`5. Resposta recebida do servidor com Status: ${resposta.status} ${resposta.statusText}`);
        // --- Fim da Depuração da Resposta ---

        if (resposta.status === 204) { // Tratamento para respostas sem conteúdo
            return { ok: resposta.ok, data: null };
        }

        const data = await resposta.json();
        return { ok: resposta.ok, data };

    } catch (erro) {
        // --- Depuração do Erro Crítico ---
        console.error("%c!!! ERRO CRÍTICO CAPTURADO PELO CATCH !!!", "color: red; font-weight: bold; font-size: 14px;");
        console.error("O erro que causou a falha na conexão foi:", erro);
        // --- Fim da Depuração do Erro Crítico ---
        return { ok: false, data: { mensagem: "Erro na conexão com o servidor." } };
    }
}
