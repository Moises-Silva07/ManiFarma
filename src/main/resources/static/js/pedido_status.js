document.addEventListener("DOMContentLoaded", async () => {
  const clienteId = localStorage.getItem("userId");
  const tabela = document.getElementById("tabelaPedidos");
  const msg = document.getElementById("message");

  if (!clienteId) {
    msg.textContent = "Erro: cliente não identificado. Faça login novamente.";
    msg.style.color = "red";
    return;
  }

  const { ok, data } = await apiRequest(`/api/pedidos/cliente/${clienteId}`, "GET", null, true, true);

  if (ok && Array.isArray(data) && data.length > 0) {
    msg.textContent = "";
    tabela.innerHTML = "";

    data.forEach(pedido => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${pedido.id}</td>
        <td>${pedido.descricao}</td>
        <td>${pedido.status || "Pendente"}</td>
        <td>${pedido.receita || "Nenhuma"}</td>
      `;
      tabela.appendChild(row);
    });

  } else if (ok && data.length === 0) {
    msg.textContent = "Você ainda não possui pedidos.";
    msg.style.color = "gray";
  } else {
    msg.textContent = "Erro ao carregar seus pedidos. Tente novamente mais tarde.";
    msg.style.color = "red";
  }
});