const clienteId = 1; // depois substituir pelo ID do cliente logado

async function carregarPedidos() {
  const { ok, data } = await apiRequest(`/api/pedidos/cliente/${clienteId}`, "GET", null, true);

  const tabela = document.getElementById("tabelaPedidos");
  tabela.innerHTML = "";

  if (!ok) {
    tabela.innerHTML = `<tr><td colspan="4">❌ Erro ao carregar pedidos</td></tr>`;
    return;
  }

  if (data.length === 0) {
    tabela.innerHTML = `<tr><td colspan="4">Nenhum pedido encontrado</td></tr>`;
    return;
  }

  data.forEach(pedido => {
    const row = `
      <tr>
        <td>${pedido.id}</td>
        <td>${pedido.descricao}</td>
        <td>${pedido.status}</td>
        <td>${pedido.receita ? pedido.receita : "Nenhum anexo"}</td>
      </tr>
    `;
    tabela.innerHTML += row;
  });
}

carregarPedidos();