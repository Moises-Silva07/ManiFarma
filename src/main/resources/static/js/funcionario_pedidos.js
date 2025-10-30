document.addEventListener("DOMContentLoaded", async () => {
  const corpoTabela = document.getElementById("corpoTabela");
  const funcionarioId = localStorage.getItem("userId"); // Funcionário logado
  if (!funcionarioId) {
    alert("Funcionário não identificado. Faça login novamente.");
    window.location.href = "/html/login/login.html";
    return;
  }

  const { ok, data } = await apiRequest("/api/pedidos", "GET", null, true, true);

  if (!ok) {
    corpoTabela.innerHTML = `<tr><td colspan="4" class="text-danger text-center">Erro ao carregar pedidos.</td></tr>`;
    return;
  }

  if (data.length === 0) {
    corpoTabela.innerHTML = `<tr><td colspan="4" class="text-muted text-center">Nenhum pedido disponível.</td></tr>`;
    return;
  }

  corpoTabela.innerHTML = "";
  data.forEach((pedido) => {
    const linha = document.createElement("tr");
    linha.innerHTML = `
      <td>${pedido.id}</td>
      <td>${pedido.employeeId || "—"}</td>
      <td>
        <span class="badge ${pedido.status === "CONCLUIDO" ? "bg-success" :
                              pedido.status === "CANCELADO" ? "bg-danger" : "bg-warning text-dark"}">
          ${pedido.status || "PENDENTE"}
        </span>
      </td>
      <td>R$ ${(pedido.valorTotal || 0).toFixed(2)}</td>
    `;

    // Subtabela de detalhes (escondida por padrão)
    const detalhes = document.createElement("tr");
    detalhes.classList.add("detalhes");
    detalhes.style.display = "none"; // mantém oculto ao carregar
    detalhes.innerHTML = `
      <td colspan="4">
        <div class="p-3 border rounded bg-light">
          <h5>Detalhes do Pedido #${pedido.id}</h5>
          <p><strong>Cliente ID:</strong> ${pedido.clienteId}</p>
          <p><strong>Descrição:</strong> ${pedido.descricao}</p>
          <p><strong>Receita:</strong> ${pedido.receita || "—"}</p>

          <h6 class="mt-3">Itens do Pedido:</h6>
          <ul>
            ${(pedido.itens || []).map(item => `
              <li>${item.produtoNome} - Quantidade: ${item.quantidade}</li>
            `).join("") || "<li>Nenhum item informado.</li>"}
          </ul>

          <div class="mt-3">
            <button class="btn btn-secondary btn-sm me-2" onclick="atribuirFuncionario(${pedido.id}, ${funcionarioId})">👤 Assumir Pedido</button>

            ${pedido.status === "PENDENTE" ? `
                <button class="btn btn-success btn-sm me-2" onclick="alterarStatus(${pedido.id}, 'VALIDO')">✅ Validar</button>
                <button class="btn btn-danger btn-sm me-2" onclick="alterarStatus(${pedido.id}, 'CANCELADO')">❎ Cancelar</button>
            ` : ""}

            ${pedido.status === "VALIDO" ? `
                <button class="btn btn-info btn-sm me-2" onclick="enviarCotacao(${pedido.id})">💲 Enviar Cotação</button>
            ` : ""}

            ${pedido.status === "PAGO" ? `
                <button class="btn btn-primary btn-sm me-2" onclick="alterarStatus(${pedido.id}, 'CONCLUIDO')">🏁 Concluir Pedido</button>
            ` : ""}
            
          </div>
        </div>
      </td>
    `;

    // Toggle ao clicar
    linha.addEventListener("click", () => {
      detalhes.style.display =
        detalhes.style.display === "none" ? "table-row" : "none";
    });

    corpoTabela.appendChild(linha);
    corpoTabela.appendChild(detalhes);
  });
});

// Função para alterar status do pedido
async function alterarStatus(id, novoStatus) {
  if (!confirm(`Deseja realmente marcar o pedido ${id} como ${novoStatus}?`)) return;

  const { ok } = await apiRequest(`/api/pedidos/${id}/status`, "PUT", { status: novoStatus }, true, true);
  if (ok) {
    alert(`Pedido ${id} atualizado para ${novoStatus}`);
    location.reload();
  } else {
    alert("Erro ao atualizar o pedido.");
  }
}

// Função para atribuir o funcionário ao pedido
async function atribuirFuncionario(pedidoId, funcionarioId) {
  const { ok } = await apiRequest(`/api/pedidos/${pedidoId}/atribuir`, "PUT", { employeeId: funcionarioId }, true, true);
  if (ok) {
    alert("Pedido atribuído com sucesso!");
    location.reload();
  } else {
    alert("Erro ao atribuir o pedido.");
  }
}

// Função para gerar o link de pagamento
async function enviarCotacao(pedidoId) {
  if (!confirm(`Deseja gerar o link de cotação e enviar ao cliente do pedido #${pedidoId}?`)) return;

  const { ok, data } = await apiRequest(`/api/pedidos/${pedidoId}/enviar-cotacao`, "POST", null, true, true);

  if (ok) {
    alert("Link de cotação gerado e e-mail enviado com sucesso!");
    location.reload();
  } else {
    alert("Erro ao gerar link de cotação: " + (data || ""));
  }
}
