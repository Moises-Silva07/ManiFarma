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

  // --- Variáveis de paginação ---
  let todosPedidos = [];
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

  // guarda todos os pedidos e renderiza a primeira página
  todosPedidos = data;
  renderizarTabela();

  // --- Função para renderizar a tabela com base na página atual ---
  function renderizarTabela() {
    corpoTabela.innerHTML = "";

    const inicio = (paginaAtual - 1) * itensPorPagina;
    const fim = inicio + itensPorPagina;
    const pedidosPagina = todosPedidos.slice(inicio, fim);

    corpoTabela.innerHTML = pedidosPagina
      .map(
        (pedido) => `
          <tr data-id="${pedido.id}">
            <td>${pedido.id}</td>
            <td>${pedido.clienteId}</td>
            <td>${pedido.employeeId || "-"}</td>
            <td>${pedido.status}</td>
            <td>R$ ${pedido.valorTotal?.toFixed(2) || "0.00"}</td>
          </tr>`
      )
      .join("");

    // Reaplica eventos de seleção
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

  // --- 3. Abrir modal com lista de produtos ---
  btnAdicionar.addEventListener("click", async () => {
    if (!pedidoSelecionado) return alert("Selecione um pedido primeiro.");

    if (produtosCache.length === 0) {
      const res = await apiRequest("/produtos", "GET", null, true, true);
      if (!res.ok) {
        alert("Erro ao carregar produtos.");
        return;
      }
      produtosCache = res.data;
    }

    quantidadesSelecionadas = {};
    listaProdutos.innerHTML = "";

    produtosCache.forEach((produto) => {
      listaProdutos.innerHTML += `
        <div class="col-12">
          <div class="produto-card" data-id="${produto.id}">
            <h5>${produto.nome}</h5>
            <p>R$ ${produto.preco.toFixed(2)}</p>
            <div class="qtd-control">
              <button class="btnMenos">−</button>
              <input type="text" class="qtdInput" value="0" readonly>
              <button class="btnMais">+</button>
            </div>
          </div>
        </div>`;
    });

    document.querySelectorAll(".produto-card").forEach((card) => {
      const id = card.getAttribute("data-id");
      const input = card.querySelector(".qtdInput");
      const btnMais = card.querySelector(".btnMais");
      const btnMenos = card.querySelector(".btnMenos");

      quantidadesSelecionadas[id] = 0;

      btnMais.addEventListener("click", () => {
        quantidadesSelecionadas[id]++;
        input.value = quantidadesSelecionadas[id];
      });

      btnMenos.addEventListener("click", () => {
        if (quantidadesSelecionadas[id] > 0) {
          quantidadesSelecionadas[id]--;
          input.value = quantidadesSelecionadas[id];
        }
      });
    });

    modal.show();
  });

  // --- 4. Confirmar e enviar ao backend ---
  btnConfirmarItens.addEventListener("click", async () => {
    const itensSelecionados = Object.entries(quantidadesSelecionadas)
      .filter(([_, qtd]) => qtd > 0)
      .map(([produtoId, quantidade]) => ({ produtoId: Number(produtoId), quantidade }));

    if (itensSelecionados.length === 0) {
      alert("Selecione ao menos 1 item!");
      return;
    }

    const response = await apiRequest(`/api/pedidos/${pedidoSelecionado}/itens`, "POST", itensSelecionados, true, true);

    if (!response.ok) {
      try {
        const text = await response.text();
        console.error("Erro ao adicionar itens - body:", text);
      } catch (e) {
        console.error("Erro ao adicionar itens - sem body legível");
      }
      alert("Erro ao adicionar itens ao pedido! Veja console para detalhes.");
      return;
    }
  });
});
