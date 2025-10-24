const apiUrl = "http://localhost:8080";


async function apiRequest(endpoint, method, body = null, auth = false ) {
    

    console.clear();
    console.log("%c--- INICIANDO NOVA REQUISIÇÃO API ---", "color: blue; font-weight: bold;");
    console.log(`1. Parâmetros recebidos: endpoint='${endpoint}', method='${method}'`);
    console.log("2. Corpo recebido (antes de converter para JSON):", body);


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


        console.log(`3. URL completa que será chamada: ${fullUrl}`);
        console.log("4. Objeto 'options' final enviado para o fetch:", options);


        const resposta = await fetch(fullUrl, options);


        console.log(`5. Resposta recebida do servidor com Status: ${resposta.status} ${resposta.statusText}`);


        if (resposta.status === 204) {
            return { ok: resposta.ok, data: null };
        }

        const data = await resposta.json();
        return { ok: resposta.ok, data };

    } catch (erro) {

        console.error("%c!!! ERRO CRÍTICO CAPTURADO PELO CATCH !!!", "color: red; font-weight: bold; font-size: 14px;");
        console.error("O erro que causou a falha na conexão foi:", erro);

        return { ok: false, data: { mensagem: "Erro na conexão com o servidor." } };
    }
}
