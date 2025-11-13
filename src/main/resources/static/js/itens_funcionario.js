let todosItens = [];
let paginaAtual = 1;
const itensPorPagina = 20;

document.addEventListener("DOMContentLoaded", carregarItens);


async function carregarItens() {
  const tabela = document.getElementById("corpoItens");
  const msg = document.getElementById("message");

  const { ok, data } = await apiRequest("/produtos", "GET");

  if (ok && Array.isArray(data)) {
    todosItens = data;
    msg.textContent = "";
    paginaAtual = 1; // sempre começa na primeira página
    renderizarTabela();
  } else {
    msg.textContent = "Erro ao carregar os itens.";
    msg.style.color = "red";
  }
}

function renderizarTabela() {
  const tabela = document.getElementById("corpoItens");
  tabela.innerHTML = "";

  // calcula faixa dos itens
  const inicio = (paginaAtual - 1) * itensPorPagina;
  const fim = inicio + itensPorPagina;
  const itensPagina = todosItens.slice(inicio, fim);

  if (itensPagina.length === 0) {
    tabela.innerHTML = `<tr><td colspan="3" class="text-center text-muted">Nenhum item cadastrado.</td></tr>`;
    return;
  }

  // monta linhas
  itensPagina.forEach(item => {
    const linha = document.createElement("tr");
    linha.dataset.id = item.id;
    linha.innerHTML = `
      <td>${item.id}</td>
      <td>${item.nome}</td>
      <td>R$ ${item.preco.toFixed(2).replace('.', ',')}</td>
    `;
    linha.addEventListener("click", () => selecionarLinha(linha));
    tabela.appendChild(linha);
  });

  renderizarPaginacao();
}

let linhaSelecionada = null;

function selecionarLinha(linha) {
  if (linhaSelecionada) {
    linhaSelecionada.classList.remove("selecionada");
  }

  linhaSelecionada = linha;
  linha.classList.add("selecionada");
}

function renderizarPaginacao() {
  const totalPaginas = Math.ceil(todosItens.length / itensPorPagina);
  const paginacao = document.getElementById("paginacao");
  paginacao.innerHTML = "";

  // Botão "Anterior"
  const anterior = document.createElement("li");
  anterior.className = `page-item ${paginaAtual === 1 ? "disabled" : ""}`;
  anterior.innerHTML = `<button class="page-link">Anterior</button>`;
  anterior.onclick = () => {
    if (paginaAtual > 1) {
      paginaAtual--;
      renderizarTabela();
    }
  };
  paginacao.appendChild(anterior);

  // Números de página (até 5 para não poluir)
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

  // Botão "Próximo"
  const proximo = document.createElement("li");
  proximo.className = `page-item ${paginaAtual === totalPaginas ? "disabled" : ""}`;
  proximo.innerHTML = `<button class="page-link">Próximo</button>`;
  proximo.onclick = () => {
    if (paginaAtual < totalPaginas) {
      paginaAtual++;
      renderizarTabela();
    }
  };
  paginacao.appendChild(proximo);
}

// --- CRUD ---
async function criarItem() {
  const nome = prompt("Digite o nome do produto:");
  const preco = parseFloat(prompt("Digite o preço do produto:").replace(",", "."));
  if (!nome || isNaN(preco)) return alert("Nome ou preço inválido.");

  const { ok } = await apiRequest("/produtos", "POST", { nome, preco });
  if (ok) {
    alert("Produto criado com sucesso!");
    carregarItens();
  } else alert("Erro ao criar produto.");
}

async function editarItem() {
  if (!linhaSelecionada) return alert("Selecione um item primeiro.");

  const id = linhaSelecionada.dataset.id;
  const nome = prompt("Novo nome do produto:");
  const preco = parseFloat(prompt("Novo preço:").replace(",", "."));
  if (!nome || isNaN(preco)) return alert("Nome ou preço inválido.");

  const { ok } = await apiRequest(`/produtos/${id}`, "PUT", { nome, preco });
  if (ok) {
    alert("Produto atualizado!");
    carregarItens();
  } else alert("Erro ao editar produto.");
}

async function excluirItem() {
  if (!linhaSelecionada) return alert("Selecione um item para excluir.");
  const id = linhaSelecionada.dataset.id;
  if (!confirm("Tem certeza que deseja excluir este item?")) return;

  const { ok } = await apiRequest(`/produtos/${id}`, "DELETE");
  if (ok) {
    alert("Produto excluído!");
    carregarItens();
  } else alert("Erro ao excluir produto.");
}
