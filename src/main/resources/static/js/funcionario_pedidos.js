// ========================================================================
//   AUTENTICAÇÃO
// ========================================================================
validarToken();

let pedidosOriginais = [];
let pedidoEmValidacao = null;
let statusEmValidacao = null;


// ========================================================================
//   FUNÇÃO GLOBAL — MODAL DE SENHA
// ========================================================================
function getModalSenhaInstance() {
  const modalEl = document.getElementById("modalSenhaFarmaceutico");
  return bootstrap.Modal.getOrCreateInstance(modalEl);
}


// ========================================================================
//   CLICK NO BOTÃO DO MODAL DE SENHA
// ========================================================================
document.getElementById("btnConfirmarSenhaFarmaceutico")
.addEventListener("click", async () => {
    const senha = document.getElementById("senhaFarmaceutico").value.trim();
    const errorMsg = document.getElementById("erroSenhaFarmaceutico");

    if (!senha) {
        errorMsg.textContent = "Informe a senha.";
        errorMsg.classList.remove("d-none");
        return;
    }

    errorMsg.classList.add("d-none");

    await enviarStatusParaAPI(pedidoEmValidacao, statusEmValidacao, senha);
});


// ========================================================================
//   FUNÇÃO PRINCIPAL QUE LIDA COM ALTERAÇÃO DE STATUS
// ========================================================================
async function alterarStatus(pedidoId, novoStatus) {

    // 1 — Buscar detalhes reais do pedido
    const { ok: okPedido, data: pedido } = await apiRequest(`/api/pedidos/${pedidoId}`, "GET", null, true, true);

    if (!okPedido) {
        return showModal({
            title: "Erro",
            message: "Não foi possível verificar o pedido.",
            type: "danger"
        });
    }

    const funcionarioLogado = Number(localStorage.getItem("userId"));

    // 2 — Bloqueio: precisa assumir antes
    if (!pedido.employeeId || pedido.employeeId !== funcionarioLogado) {
        return showModal({
            title: "Ação Bloqueada",
            message: "Você precisa ASSUMIR o pedido antes.",
            type: "warning"
        });
    }

    // 3 — Confirmação inicial
    const confirmar = await showModal({
        title: "Confirmar Ação",
        message: `Deseja realmente alterar o status para ${novoStatus}?`,
        type: "confirm"
    });

    if (!confirmar) return;

    // 4 — CASO SEJA "VALIDO", abrir modal de senha
    if (novoStatus === "VALIDO") {
        pedidoEmValidacao = pedidoId;
        statusEmValidacao = novoStatus;

        document.getElementById("senhaFarmaceutico").value = "";
        document.getElementById("erroSenhaFarmaceutico").classList.add("d-none");

        getModalSenhaInstance().show();
        return;
    }

    // 5 — Se NÃO for VALIDO → enviar direto
    await enviarStatusParaAPI(pedidoId, novoStatus, null);
}


// ========================================================================
//   FUNÇÃO QUE REALMENTE ENVIA O STATUS PARA O BACK-END
// ========================================================================
async function enviarStatusParaAPI(pedidoId, status, senha) {

    const payload = { status };
    if (senha) payload.senha = senha;

    const { ok, data, statusCode } = await apiRequest(
        `/api/pedidos/${pedidoId}/status`,
        "PUT",
        payload,
        true,
        true
    );

    // Fechar modal se estiver aberto
    try { getModalSenhaInstance().hide(); } catch (e) {}

    if (ok) {
      // Exibir modal de sucesso
      await showModal({
          title: "Sucesso",
          message: `Status alterado para ${status}.`,
          type: "success"
      });

    return;
}

    // ERRO: senha incorreta
    if (statusCode === 403) {
        const errorMsg = document.getElementById("erroSenhaFarmaceutico");
        errorMsg.textContent = data?.error || "Senha incorreta.";
        errorMsg.classList.remove("d-none");

        getModalSenhaInstance().show();
        return;
    }

    showModal({
        title: "Erro",
        message: data?.error || "Erro ao alterar status.",
        type: "danger"
    });
}



// ========================================================================
//   INÍCIO DO SCRIPT PRINCIPAL DA PÁGINA
// ========================================================================
document.addEventListener("DOMContentLoaded", async () => {

  const corpoTabela = document.getElementById("corpoTabela");
  const funcionarioId = localStorage.getItem("userId");
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

  let todosPedidos = [];
  let paginaAtual = 1;
  const itensPorPagina = 8;

  const { ok, data } = await apiRequest("/api/pedidos", "GET", null, true, true);

  if (!ok) {
    corpoTabela.innerHTML = `<tr><td colspan="4" class="text-danger text-center">Erro ao carregar pedidos.</td></tr>`;
    return;
  }

  if (data.length === 0) {
    corpoTabela.innerHTML = `<tr><td colspan="4" class="text-muted text-center">Nenhum pedido encontrado.</td></tr>`;
    return;
  }

  todosPedidos = data;
  pedidosOriginais = [...data];

  renderizarTabela();

  // ========================================================================
  //   FILTROS
  // ========================================================================
  document.getElementById("btnAplicarFiltro").addEventListener("click", () => {
      const tipo = document.getElementById("tipoFiltro").value;
      const valor = Number(document.getElementById("valorFiltro").value);
      const valorStatus = document.getElementById("valorStatus").value;

      if (tipo !== "status" && !valor) {
          return showModal({
              title: "Aviso",
              message: "Digite um ID válido para filtrar.",
              type: "warning"
          });
      }

      if (tipo === "status" && !valorStatus) {
          return showModal({
              title: "Aviso",
              message: "Selecione um status.",
              type: "warning"
          });
      }

      let filtrados = pedidosOriginais;

      if (tipo === "pedido") filtrados = filtrados.filter(p => p.id === valor);
      if (tipo === "cliente") filtrados = filtrados.filter(p => p.clienteId === valor);
      if (tipo === "funcionario") filtrados = filtrados.filter(p => p.employeeId === valor);
      if (tipo === "status") filtrados = filtrados.filter(p => p.status === valorStatus);

      todosPedidos = filtrados;
      paginaAtual = 1;
      renderizarTabela();
  });

  document.getElementById("tipoFiltro").addEventListener("change", () => {
      const tipo = document.getElementById("tipoFiltro").value;
      const campoId = document.getElementById("valorFiltro").parentElement;
      const campoStatus = document.getElementById("filtroStatusContainer");

      if (tipo === "status") {
          campoId.classList.add("d-none");
          campoStatus.classList.remove("d-none");
      } else {
          campoId.classList.remove("d-none");
          campoStatus.classList.add("d-none");
      }
  });

  document.getElementById("btnLimparFiltro").addEventListener("click", () => {
      todosPedidos = [...pedidosOriginais];
      paginaAtual = 1;

      document.getElementById("tipoFiltro").value = "";
      document.getElementById("valorFiltro").value = "";
      document.getElementById("valorStatus").value = "";

      document.getElementById("filtroStatusContainer").classList.add("d-none");
      renderizarTabela();
  });

  // ========================================================================
  //   RENDERIZAR TABELA
  // ========================================================================
  function renderizarTabela() {
    corpoTabela.innerHTML = "";

    const inicio = (paginaAtual - 1) * itensPorPagina;
    const fim = inicio + itensPorPagina;
    const pedidosPagina = todosPedidos.slice(inicio, fim);

    pedidosPagina.forEach((pedido) => {
      const linha = document.createElement("tr");
      linha.innerHTML = `
        <td>${pedido.id}</td>
        <td>${pedido.employeeId ? `${pedido.employeeId} - ${pedido.employeeNome}` : "—"}</td>
        <td>
          <span class="badge ${
            pedido.status === "CONCLUIDO" ? "bg-success" :
            pedido.status === "CANCELADO" ? "bg-danger" :
            "bg-warning text-dark"
          }">${pedido.status}</span>
        </td>
        <td>R$ ${(pedido.valorTotal || 0).toFixed(2)}</td>
      `;

      // detalhes
      const detalhes = document.createElement("tr");
      detalhes.style.display = "none";
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
              <button class="btn btn-outline-primary btn-sm ver-receita" data-id="${pedido.id}">
                📄 Ver Receita
              </button>

              <button class="btn btn-secondary btn-sm" onclick="atribuirFuncionario(${pedido.id}, ${funcionarioId})">
                👤 Assumir Pedido
              </button>

              ${pedido.status === "PENDENTE" ? `
                 <button class="btn btn-success btn-sm" onclick="alterarStatus(${pedido.id}, 'VALIDO')">✅ Validar</button>
                 <button class="btn btn-danger btn-sm" onclick="alterarStatus(${pedido.id}, 'CANCELADO')">❎ Cancelar</button>
              ` : ""}

              ${pedido.status === "VALIDO" ? `
                 <button class="btn btn-info btn-sm" onclick="enviarCotacao(${pedido.id})">💲 Enviar Cotação</button>
              ` : ""}

              ${pedido.status === "PAGO" ? `
                 <button class="btn btn-primary btn-sm" onclick="alterarStatus(${pedido.id}, 'CONCLUIDO')">🏁 Concluir Pedido</button>
              ` : ""}
            </div>
          </div>
        </td>
      `;

      linha.addEventListener("click", () => {
        detalhes.style.display = detalhes.style.display === "none" ? "table-row" : "none";
      });

      corpoTabela.appendChild(linha);
      corpoTabela.appendChild(detalhes);
    });

    renderizarPaginacao();
    inicializarVerReceita();
  }


  // ========================================================================
  //   PAGINAÇÃO
  // ========================================================================
  function renderizarPaginacao() {
    paginacao.innerHTML = "";
    const totalPaginas = Math.ceil(todosPedidos.length / itensPorPagina);

    const liAnterior = document.createElement("li");
    liAnterior.className = `page-item ${paginaAtual === 1 ? "disabled" : ""}`;
    liAnterior.innerHTML = `<button class="page-link">Anterior</button>`;
    liAnterior.onclick = () => {
      paginaAtual--;
      renderizarTabela();
    };
    paginacao.appendChild(liAnterior);

    for (let i = 1; i <= totalPaginas; i++) {
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
      paginaAtual++;
      renderizarTabela();
    };
    paginacao.appendChild(liProximo);
  }


  // ========================================================================
  //   VISUALIZAR RECEITA
  // ========================================================================
  function inicializarVerReceita() {
    document.querySelectorAll(".ver-receita").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        e.stopPropagation();

        const pedidoId = e.currentTarget.getAttribute("data-id");
        const img = document.getElementById("imagemReceita");
        const msgErro = document.getElementById("mensagemErroReceita");
        const modal = new bootstrap.Modal(document.getElementById("modalReceita"));

        img.src = "";
        msgErro.classList.add("d-none");

        try {
          const token = localStorage.getItem("token");
          const response = await fetch(`/api/pedidos/${pedidoId}/receita`, {
            headers: { "Authorization": `Bearer ${token}` }
          });

          if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            msgErro.textContent = data.error || "Erro ao carregar imagem.";
            msgErro.classList.remove("d-none");
          } else {
            const blob = await response.blob();
            img.src = URL.createObjectURL(blob);
          }

        } catch (err) {
          msgErro.textContent = "Erro ao carregar receita.";
          msgErro.classList.remove("d-none");
        }

        modal.show();
      });
    });
  }

});

// ========================================================================
//   ATRIBUIR FUNCIONÁRIO
// ========================================================================
async function atribuirFuncionario(pedidoId, funcionarioId) {

  const confirmar = await showModal({
    title: "Assumir Pedido",
    message: `Deseja assumir o pedido #${pedidoId}?`,
    type: "confirm"
  });

  if (!confirmar) return;

  const { ok } = await apiRequest(
    `/api/pedidos/${pedidoId}/atribuir`,
    "PUT",
    { employeeId: funcionarioId },
    true,
    true
  );

  if (ok) {
    await showModal({
      title: "Sucesso",
      message: "Pedido atribuído!",
      type: "success"
    });
    
  } else {
    showModal({
      title: "Erro",
      message: "Erro ao atribuir pedido.",
      type: "danger"
    });
  }
}


// ========================================================================
//   ENVIAR COTAÇÃO
// ========================================================================
async function enviarCotacao(pedidoId) {

  const confirmar = await showModal({
    title: "Enviar Cotação",
    message: `Gerar e enviar cotação do pedido #${pedidoId}?`,
    type: "confirm"
  });

  if (!confirmar) return;

  const { ok, data } = await apiRequest(
    `/api/pedidos/${pedidoId}/enviar-cotacao`,
    "POST",
    null, true, true
  );

  if (ok) {
    await showModal({
      title: "Sucesso",
      message: "Cotação enviada!",
      type: "success"
    });
  } else {
    showModal({
      title: "Erro",
      message: data?.error || "Erro ao enviar cotação.",
      type: "danger"
    });
  }
}
