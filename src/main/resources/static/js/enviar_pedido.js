// Verifica se o usuário está logado
const userId = localStorage.getItem("userId");
if (!userId) {
    alert("Usuário não identificado. Faça login novamente.");
    window.location.href = "/html/login/login.html";
}

// Evento de envio do formulário
document.getElementById("form").addEventListener("submit", async (e) => {
    e.preventDefault();

    // --- 1. Captura os valores do formulário ---
    const descricao = document.getElementById("descricao").value.trim();
    const receitaInput = document.getElementById("receita");
    const arquivo = receitaInput.files[0]; // Pega o arquivo

    if (!descricao || !arquivo) {
        alert("Por favor, preencha a descrição e selecione um arquivo de receita.");
        return;
    }

    // --- 2. Por enquanto, apenas convertendo o arquivo em string (simulação) ---
    // Futuramente faremos upload multipart real.
    const receitaSimulada = arquivo.name; // Ex: "receita.pdf" (temporário)

    // --- 3. Monta o corpo da requisição ---
    const body = {
        descricao: descricao,
        receita: receitaSimulada, // por enquanto só o nome do arquivo
        clienteId: parseInt(userId),
        employeeId: null, // funcionário ainda não definido no momento
        itens: [] // pode deixar vazio agora
    };

    // --- 4. Envia a requisição ---
    const resposta = await apiRequest("/api/pedidos", "POST", body, true, true);

    // --- 5. Trata a resposta ---
    if (resposta.ok) {
        alert("Pedido enviado com sucesso!");
        document.getElementById("form").reset();
    } else {
        console.error("Erro ao enviar pedido:", resposta.data);
        alert("Erro ao enviar pedido. Verifique os dados e tente novamente.");
    }
});