document.addEventListener("DOMContentLoaded", async () => {
  const corpoTabela = document.getElementById("corpoTabela");
  const btnAdicionar = document.getElementById("btnAdicionarItens");
  const modal = new bootstrap.Modal(document.getElementById("modalItens"));
  const listaProdutos = document.getElementById("listaProdutos");
  const btnConfirmarItens = document.getElementById("btnConfirmarItens");

  let pedidoSelecionado = null;
  let produtosCache = [];
  let quantidadesSelecionadas = {};

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

  corpoTabela.innerHTML = data
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

  // --- 2. Selecionar pedido ---
  document.querySelectorAll("#corpoTabela tr").forEach((linha) => {
    linha.addEventListener("click", () => {
      document.querySelectorAll("#corpoTabela tr").forEach((l) => l.classList.remove("selecionada"));
      linha.classList.add("selecionada");
      pedidoSelecionado = linha.getAttribute("data-id");
      btnAdicionar.disabled = false;
    });
  });

  // --- 3. Abrir modal com lista de produtos ---
  btnAdicionar.addEventListener("click", async () => {
    if (!pedidoSelecionado) return alert("Selecione um pedido primeiro.");

    // Busca produtos apenas uma vez
    if (produtosCache.length === 0) {
      const res = await apiRequest("/produtos", "GET", null, true, true);
      if (!res.ok) {
        alert("Erro ao carregar produtos.");
        return;
      }
      produtosCache = res.data;
    }

    // Limpa estado anterior
    quantidadesSelecionadas = {};
    listaProdutos.innerHTML = "";

    // Monta cards de produtos
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

    // Adiciona eventos + e −
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

    // corrigido: inclui o /api para bater com @RequestMapping("/api/pedidos")
    const response = await apiRequest(`/api/pedidos/${pedidoSelecionado}/itens`, "POST", itensSelecionados, true, true);


    if (!response.ok) {
      // tentar ler corpo de erro e logar (se apiRequest retornar o Response)
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
