// Verifica se o usuário está logado com Token
validarToken(); 

let pedidosOriginais = []; // manter todos os pedidos 

document.addEventListener("DOMContentLoaded", async () => {

  const corpoTabela = document.getElementById("corpoTabela");
  const funcionarioId = localStorage.getItem("userId"); // Funcionário logado
  const paginacao = document.getElementById("paginacao");

  if (!funcionarioId) {
    await showModal({
      title: "Aviso",
      message: "Funcionário não identificado. Faça login novamente.",
      type: "warning"
    });
    window.location.href = "/html/login/login.html";
    return;
  }

  // Variáveis de paginação
  let todosPedidos = [];
  let paginaAtual = 1;
  const itensPorPagina = 8;

  const { ok, data } = await apiRequest("/api/pedidos", "GET", null, true, true);

  if (!ok) {
    corpoTabela.innerHTML = `<tr><td colspan="4" class="text-danger text-center">Erro ao carregar pedidos.</td></tr>`;
    return;
  }

  if (data.length === 0) {
    corpoTabela.innerHTML = `<tr><td colspan="4" class="text-muted text-center">Nenhum pedido disponível.</td></tr>`;
    return;
  }

  todosPedidos = data;
  pedidosOriginais = [...data];
  renderizarTabela();


  // ==============================
  // FILTRAR PEDIDOS
  // ==============================
  document.getElementById("btnAplicarFiltro").addEventListener("click", () => {
      const tipo = document.getElementById("tipoFiltro").value;
      const valor = Number(document.getElementById("valorFiltro").value);

      if (!valor) {
          showModal({
              title: "Aviso",
              message: "Digite um ID válido para filtrar.",
              type: "warning"
          });
          return;
      }

      let filtrados = pedidosOriginais;

      if (tipo === "pedido") {
          filtrados = filtrados.filter(p => p.id === valor);
      }
      if (tipo === "cliente") {
          filtrados = filtrados.filter(p => p.clienteId === valor);
      }
      if (tipo === "funcionario") {
          filtrados = filtrados.filter(p => p.employeeId === valor);
      }

      todosPedidos = filtrados;
      paginaAtual = 1;
      renderizarTabela();
  });

  // LIMPAR FILTRO
  document.getElementById("btnLimparFiltro").addEventListener("click", () => {
      todosPedidos = [...pedidosOriginais];
      paginaAtual = 1;
      document.getElementById("valorFiltro").value = "";
      renderizarTabela();
  });


  // --- Função para renderizar tabela ---
  function renderizarTabela() {
    corpoTabela.innerHTML = "";

    const inicio = (paginaAtual - 1) * itensPorPagina;
    const fim = inicio + itensPorPagina;
    const pedidosPagina = todosPedidos.slice(inicio, fim);

    pedidosPagina.forEach((pedido) => {
      const linha = document.createElement("tr");
      linha.innerHTML = `
        <td>${pedido.id}</td>
        <td>
          ${pedido.employeeId ? `${pedido.employeeId} - ${pedido.employeeNome}` : "—"}
        </td>
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
          <p><strong>Funcionário responsável:</strong> 
            ${pedido.employeeNome ? pedido.employeeNome + " (ID: " + pedido.employeeId + ")" : "Nenhum funcionário atribuído."}
          </p>
          <br>
          <p><strong>Cliente ID:</strong> ${pedido.clienteId}</p>
          <p><strong>Cliente:</strong> ${pedido.clienteNome || "—"}</p>
          <p><strong>Telefone:</strong> ${pedido.clienteTelefone || "—"}</p>
          <br>
          <p><strong>Descrição:</strong> ${pedido.descricao}</p>
          <p><strong>Receita:</strong> ${pedido.receita || "—"}</p>

          <h6 class="mt-3">Itens do Pedido:</h6>
          <ul>
            ${(pedido.itens || []).map(item => `
              <li>${item.produtoNome} - Quantidade: ${item.quantidade}</li>
            `).join("") || "<li>Nenhum item informado.</li>"}
          </ul>

          <div class="mt-3">
            <button class="btn btn-outline-primary btn-sm me-2 ver-receita" data-id="${pedido.id}">
              📄 Ver Receita
            </button>

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
      detalhes.style.display = detalhes.style.display === "none" ? "table-row" : "none";
    });

    corpoTabela.appendChild(linha);
    corpoTabela.appendChild(detalhes);
  });

  renderizarPaginacao();
  inicializarVerReceita();
}

  // --- Função para renderizar os botões de paginação ---
  function renderizarPaginacao() {
    paginacao.innerHTML = "";
    const totalPaginas = Math.ceil(todosPedidos.length / itensPorPagina);

    const liAnterior = document.createElement("li");
    liAnterior.className = `page-item ${paginaAtual === 1 ? "disabled" : ""}`;
    liAnterior.innerHTML = `<button class="page-link">Anterior</button>`;
    liAnterior.onclick = () => {
      if (paginaAtual > 1) {
        paginaAtual--;
        renderizarTabela();
      }
    };
    paginacao.appendChild(liAnterior);

    const inicio = Math.max(1, paginaAtual - 2);
    const fim = Math.min(totalPaginas, inicio + 4);
    for (let i = inicio; i <= fim; i++) {
      const li = document.createElement("li");
      li.className = `page-item ${i === paginaAtual ? "active" : ""}`;
      li.innerHTML = `<button class="page-link">${i}</button>`;
      li.onclick = () => {
        paginaAtual = i;
        renderizarTabela();
      };
      paginacao.appendChild(li);
    }

    const liProximo = document.createElement("li");
    liProximo.className = `page-item ${paginaAtual === totalPaginas ? "disabled" : ""}`;
    liProximo.innerHTML = `<button class="page-link">Próximo</button>`;
    liProximo.onclick = () => {
      if (paginaAtual < totalPaginas) {
        paginaAtual++;
        renderizarTabela();
      }
    };
    paginacao.appendChild(liProximo);
  }

  // --- Reatribui os botões "Ver Receita" após renderização ---
  function inicializarVerReceita() {
    document.querySelectorAll(".ver-receita").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        e.stopPropagation(); // evita abrir/fechar detalhes ao clicar no botão

        const pedidoId = e.currentTarget.getAttribute("data-id");
        const img = document.getElementById("imagemReceita");
        const msgErro = document.getElementById("mensagemErroReceita");
        const modal = new bootstrap.Modal(document.getElementById("modalReceita"));

        img.src = "";
        msgErro.classList.add("d-none");

        try {
          const token = localStorage.getItem("token");

          const response = await fetch(`http://localhost:8080/api/pedidos/${pedidoId}/receita`, {
            method: "GET",
            headers: {
              "Authorization": `Bearer ${token}` // ✔ ENVIA O TOKEN
            }
          });

          if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            msgErro.textContent = data.error || "Erro ao carregar a imagem.";
            msgErro.classList.remove("d-none");
          } else {
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            img.src = url;
          }

          modal.show();
        } catch (error) {
          msgErro.textContent = "Erro ao buscar imagem do servidor.";
          msgErro.classList.remove("d-none");
          modal.show();
        }
      });
    });
  }
});


// Função alterar Status
async function alterarStatus(id, novoStatus) {
  const confirmar = await showModal({
    title: "Confirmar Ação",
    message: `Deseja realmente marcar o pedido ${id} como ${novoStatus}?`,
    type: "confirm"
  });

  if (!confirmar) return;

  const { ok } = await apiRequest(`/api/pedidos/${id}/status`, "PUT", { status: novoStatus }, true, true);

  if (ok) {
    await showModal({
      title: "Sucesso!",
      message: `Pedido ${id} atualizado para ${novoStatus}.`,
      type: "success"
    });

    
  } else {
    showModal({
      title: "Erro",
      message: "Erro ao atualizar o pedido.",
      type: "danger"
    });
  }
}

// Função Atribuir Funcionario
async function atribuirFuncionario(pedidoId, funcionarioId) {
  const { ok } = await apiRequest(`/api/pedidos/${pedidoId}/atribuir`,
    "PUT",
    { employeeId: funcionarioId },
    true,
    true
  );

  if (ok) {
    await showModal({
      title: "Atribuição Realizada",
      message: `O pedido ${pedidoId} agora está sob sua responsabilidade.`,
      type: "success"
    });

  } else {
    showModal({
      title: "Erro",
      message: "Erro ao atribuir o pedido.",
      type: "danger"
    });
  }
}

// Função para gerar o link de cotação
async function enviarCotacao(pedidoId) {
  const confirmar = await showModal({
    title: "Gerar Cotação",
    message: `Deseja gerar e enviar a cotação do pedido #${pedidoId}?`,
    type: "confirm"
  });

  if (!confirmar) return;

  const { ok, data } = await apiRequest(
    `/api/pedidos/${pedidoId}/enviar-cotacao`,
    "POST",
    null,
    true,
    true
  );

  if (ok) {
    await showModal({
      title: "Cotação Enviada",
      message: "O link foi gerado e enviado ao cliente!",
      type: "success"
    });
  } else {
    showModal({
      title: "Erro",
      message: "Erro ao gerar/enviar a cotação: " + (data || ""),
      type: "danger"
    });
  }
}

