// Verifica se o usuário está logado com Token
validarToken(); 

document.addEventListener("DOMContentLoaded", async () => {
  const corpoTabela = document.getElementById("corpoTabela");
  const btnAdicionar = document.getElementById("btnAdicionarItens");
  const modal = new bootstrap.Modal(document.getElementById("modalItens"));
  const listaProdutos = document.getElementById("listaProdutos");
  const btnConfirmarItens = document.getElementById("btnConfirmarItens");
  const paginacao = document.getElementById("paginacao");

  let pedidoSelecionado = null;
  let produtosCache = [];
  let quantidadesSelecionadas = {};
  let dosesSelecionadas = {};

  // --- Variáveis de paginação ---
  let todosPedidos = [];
  let pedidosOriginais = [];
  let paginaAtual = 1;
  const itensPorPagina = 10;

  // --- 1. Carrega pedidos com status VALIDO ---
  const { ok, data } = await apiRequest("/api/pedidos/status/VALIDO", "GET", null, true, true);
  if (!ok) {
    corpoTabela.innerHTML = `<tr><td colspan="5" class="text-danger text-center">Erro ao carregar pedidos.</td></tr>`;
    return;
  }

  if (data.length === 0) {
    corpoTabela.innerHTML = `<tr><td colspan="5" class="text-muted text-center">Nenhum pedido disponível.</td></tr>`;
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

  // --- Função para renderizar a tabela com base na página atual ---
  function renderizarTabela() {
    corpoTabela.innerHTML = "";

    const inicio = (paginaAtual - 1) * itensPorPagina;
    const fim = inicio + itensPorPagina;
    const pedidosPagina = todosPedidos.slice(inicio, fim);

    if (pedidosPagina.length === 0) {
      corpoTabela.innerHTML = `<tr><td colspan="5" class="text-muted text-center">Nenhum pedido encontrado.</td></tr>`;
      return;
    }

    corpoTabela.innerHTML = pedidosPagina
      .map(
        (pedido) => `
          <tr data-id="${pedido.id}">
            <td>${pedido.id}</td>
            <td>${pedido.clienteId ? `${pedido.clienteId} - ${pedido.clienteNome}` : "—"}</td>
            <td>${pedido.employeeId ? `${pedido.employeeId} - ${pedido.employeeNome}` : "—"}</td>
            <td>${pedido.status}</td>
            <td>R$ ${(pedido.valorTotal ?? 0).toFixed(2)}</td>
          </tr>`
      )
      .join("");

    document.querySelectorAll("#corpoTabela tr").forEach((linha) => {
      linha.addEventListener("click", () => {
        document.querySelectorAll("#corpoTabela tr").forEach((l) => l.classList.remove("selecionada"));
        linha.classList.add("selecionada");
        pedidoSelecionado = linha.getAttribute("data-id");
        btnAdicionar.disabled = false;
      });
    });

    renderizarPaginacao();
  }

  // --- Função para renderizar os botões de paginação ---
  function renderizarPaginacao() {
    paginacao.innerHTML = "";
    const totalPaginas = Math.ceil(todosPedidos.length / itensPorPagina);

    if (totalPaginas <= 1) return;

    // Botão Anterior
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

    // Páginas numéricas (máximo 5)
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

    // Botão Próximo
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

  // --- 3. Abrir modal com lista de produtos (agora com dose/unidade) ---
  btnAdicionar.addEventListener("click", async () => {
    if (!pedidoSelecionado) {
      return showModal({
        title: "Atenção",
        message: "Selecione um pedido primeiro.",
        type: "warning"
      });
    }

    if (produtosCache.length === 0) {
      const res = await apiRequest("/produtos", "GET", null, true, true);
      if (!res.ok) {
        showModal({
            title: "Erro",
            message: "Erro ao carregar produtos.",
            type: "danger",
        });
        return;
      }
      produtosCache = res.data;
    }

    quantidadesSelecionadas = {};
    dosesSelecionadas = {};
    listaProdutos.innerHTML = "";

    produtosCache.forEach((produto) => {
      const unidade = produto.unidade || "MG";
      const preco = produto.precoPorUnidade ?? produto.preco ?? 0;

      listaProdutos.innerHTML += `
        <div class="col-12">
          <div class="produto-card" data-id="${produto.id}" data-unidade="${unidade}">
            <div>
              <h5>${produto.nome}</h5>
              <p class="mb-1">
                Unidade: <strong>${unidade}</strong><br>
                Preço por unidade: <strong>R$ ${Number(preco).toFixed(4)}</strong> / ${unidade.toLowerCase()}
              </p>
            </div>

            <div class="mt-2">
              <label class="form-label mb-1">Dose (${unidade.toLowerCase()}):</label>
              <input type="number" class="form-control doseInput" step="0.01" min="0">
            </div>

            <div class="mt-2">
              <label class="form-label mb-1">Quantidade (frascos/unidades):</label>
              <div class="qtd-control">
                <button type="button" class="btnMenos">−</button>
                <input type="text" class="qtdInput" value="0" readonly>
                <button type="button" class="btnMais">+</button>
              </div>
            </div>
          </div>
        </div>`;
    });

    document.querySelectorAll(".produto-card").forEach((card) => {
      const id = card.getAttribute("data-id");
      const inputQtd = card.querySelector(".qtdInput");
      const btnMais = card.querySelector(".btnMais");
      const btnMenos = card.querySelector(".btnMenos");
      const inputDose = card.querySelector(".doseInput");

      quantidadesSelecionadas[id] = 0;
      dosesSelecionadas[id] = 0;

      btnMais.addEventListener("click", () => {
        quantidadesSelecionadas[id]++;
        inputQtd.value = quantidadesSelecionadas[id];
      });

      btnMenos.addEventListener("click", () => {
        if (quantidadesSelecionadas[id] > 0) {
          quantidadesSelecionadas[id]--;
          inputQtd.value = quantidadesSelecionadas[id];
        }
      });

      inputDose.addEventListener("input", () => {
        const v = parseFloat(inputDose.value);
        dosesSelecionadas[id] = isNaN(v) ? 0 : v;
      });
    });

    modal.show();
  });

  // --- 4. Confirmar e enviar ao backend ---
  btnConfirmarItens.addEventListener("click", async () => {

    const itensSelecionados = [];

    for (const [produtoId, dose] of Object.entries(dosesSelecionadas)) {
      const d = Number(dose);
      if (d > 0) {
        const qtd = quantidadesSelecionadas[produtoId] && quantidadesSelecionadas[produtoId] > 0
          ? quantidadesSelecionadas[produtoId]
          : 1; // default 1

        const produto = produtosCache.find(p => String(p.id) === String(produtoId));
        const unidade = produto && produto.unidade ? produto.unidade : "MG";

        itensSelecionados.push({
          produtoId: Number(produtoId),
          quantidade: qtd,
          dose: d,
          unidade: unidade
        });
      }
    }

    if (itensSelecionados.length === 0) {
      return showModal({
        title: "Atenção",
        message: "Informe pelo menos a dose de um produto para adicionar.",
        type: "warning",
      });
    }

    const response = await apiRequest(
        `/api/pedidos/${pedidoSelecionado}/itens`,
        "POST",
        itensSelecionados,
        true,
        true
    );

    if (!response.ok) {
        return showModal({
            title: "Erro",
            message: "Não foi possível adicionar os itens ao pedido.",
            type: "danger",
        });
    }

    showModal({
        title: "Sucesso!",
        message: "Itens adicionados ao pedido com sucesso!",
        type: "success",
    });

    modal.hide();
  });
});
