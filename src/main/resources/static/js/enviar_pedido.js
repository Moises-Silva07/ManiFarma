document.getEkementById("pedidoForm").addEventListener("submit", async (e) => {
    
    e.preventDefault();

    const descricao = document.getElementById("descricao").value;
    const clienteId = document.getElementById("clienteId").value;
    const receitaFile = document.getElementById("receita").files[0];

    const requestBody = {
        descricao,
        status: "PENDENTE",
        receita: receitaFile ? receitaFile.name : null, // apenas nome do arquivo
        clienteId: parseInt(clienteId)
    };

    const {ok, data} = await apiRequest(
        "api/pedidos/criar",
        "POST",
        requestBody,
        true
    );
    
    const msg = document.getElementById("mensagem");
  if (ok) {
    msg.textContent = "Pedido enviado com sucesso! ID: " + data.id;
    msg.style.color = "green";
  } else {
    msg.textContent = "Erro: " + (data.mensagem || "Não foi possível enviar o pedido.");
    msg.style.color = "red";
  }

});