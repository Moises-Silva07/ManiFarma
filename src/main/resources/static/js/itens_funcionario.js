// Verifica se o usuário está logado com Token
// validarToken(); 

let itensOriginais = []; // salva todos os itens sem filtro

// ==============================================
// VARIÁVEIS GLOBAIS
// ==============================================
let todosItens = [];
let paginaAtual = 1;
const itensPorPagina = 20;

let linhaSelecionada = null; // linha da tabela selecionada
let modoEdicao = false;      // false = criando, true = editando
let idEdicao = null;         // armazena id do item sendo editado


// ==============================================
// INICIALIZAÇÃO
// ==============================================
document.addEventListener("DOMContentLoaded", carregarItens);


// ==============================================
// 1. CARREGAR ITENS DO BACK-END
// ==============================================
async function carregarItens() {
  const msg = document.getElementById("message");

  const { ok, data } = await apiRequest("/produtos", "GET");

  if (ok && Array.isArray(data)) {
    todosItens = data;
    itensOriginais = [...data]; // salva lista para restaurar depois
    msg.textContent = "";
    paginaAtual = 1;
    renderizarTabela();
  } else {
    showModal({
      title: "Erro",
      message: "Erro ao carregar os itens.",
      type: "danger",
    });
  }
}


// ==============================================
// 2. RENDERIZA TABELA COM PAGINAÇÃO
// ==============================================
function renderizarTabela() {
  const tabela = document.getElementById("corpoItens");
  tabela.innerHTML = "";

  const inicio = (paginaAtual - 1) * itensPorPagina;
  const fim = inicio + itensPorPagina;
  const itensPagina = todosItens.slice(inicio, fim);

  if (itensPagina.length === 0) {
    tabela.innerHTML = `
      <tr><td colspan="3" class="text-center text-muted">
        Nenhum item cadastrado.
      </td></tr>`;
    return;
  }

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


// ==============================================
// 3. SELECIONAR UMA LINHA DA TABELA
// ==============================================
function selecionarLinha(linha) {
  if (linhaSelecionada) linhaSelecionada.classList.remove("selecionada");

  linhaSelecionada = linha;
  linha.classList.add("selecionada");
}


// ==============================================
// 4. PAGINAÇÃO
// ==============================================
function renderizarPaginacao() {
  const totalPaginas = Math.ceil(todosItens.length / itensPorPagina);
  const paginacao = document.getElementById("paginacao");
  paginacao.innerHTML = "";

  // Botão Anterior
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

  // Números
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


// ==============================================
// 5. CRUD - CRIAR ITEM
// ==============================================
// Abrir modal no modo CRIAÇÃO
function criarItem() {
  abrirModalItem(true);
}


// ==============================================
// 6. CRUD - EDITAR ITEM
// ==============================================
async function editarItem() {
  if (!linhaSelecionada) {
    return showModal({
      title: "Atenção",
      message: "Selecione um item primeiro.",
      type: "warning"
    });
  }

  const id = linhaSelecionada.dataset.id;
  const item = todosItens.find(i => i.id == id);

  abrirModalItem(false, item);
}


// ==============================================
// 7. CRUD - EXCLUIR ITEM
// ==============================================
async function excluirItem() {
  if (!linhaSelecionada) {
    return showModal({
      title: "Atenção",
      message: "Selecione um item para excluir.",
      type: "warning",
    });
  }

  const id = linhaSelecionada.dataset.id;

  const confirmar = await showModal({
    title: "Confirmação",
    message: "Tem certeza que deseja excluir este item?",
    type: "confirm"
  });

  if (!confirmar) return;

  const { ok } = await apiRequest(`/produtos/${id}`, "DELETE");

  if (ok) {
    showModal({
      title: "Sucesso!",
      message: "Produto excluído com sucesso!",
      type: "success"
    });
    carregarItens();
  } else {
    showModal({
      title: "Erro",
      message: "Erro ao excluir produto.",
      type: "danger"
    });
  }
}


// ==============================================
// 8. FUNÇÃO QUE ABRE O MODAL DE CRIAÇÃO / EDIÇÃO
// ==============================================
function abrirModalItem(criando, dados = null) {
  modoEdicao = !criando;
  idEdicao = dados ? dados.id : null;

  document.getElementById("tituloItemModal").textContent =
    criando ? "Cadastrar Item" : "Editar Item";

  document.getElementById("itemNome").value = dados ? dados.nome : "";
  document.getElementById("itemPreco").value = dados ? dados.preco : "";

  const modal = new bootstrap.Modal(document.getElementById("modalItemForm"));
  modal.show();
}


// ==============================================
// 9. BOTÃO SALVAR (CRIA ou EDITA)
// ==============================================
document.getElementById("btnSalvarItem").addEventListener("click", async () => {
  const nome = document.getElementById("itemNome").value.trim();
  const preco = parseFloat(document.getElementById("itemPreco").value);

  if (!nome || isNaN(preco)) {
    return showModal({
      title: "Erro",
      message: "Preencha todos os campos corretamente.",
      type: "warning"
    });
  }

  let resposta;

  if (modoEdicao) {
    resposta = await apiRequest(`/produtos/${idEdicao}`, "PUT", { nome, preco });
  } else {
    resposta = await apiRequest(`/produtos`, "POST", { nome, preco });
  }

  if (resposta.ok) {
    showModal({
      title: "Sucesso!",
      message: modoEdicao ? "Produto atualizado!" : "Produto criado com sucesso!",
      type: "success"
    });

    carregarItens();

    // fechar modal
    bootstrap.Modal.getInstance(document.getElementById("modalItemForm")).hide();
  } else {
    showModal({
      title: "Erro",
      message: "Não foi possível salvar o item.",
      type: "danger"
    });
  }
});

// ==============================================
// 10. FILTRO DE ITENS POR ID
// ==============================================

document.getElementById("btnAplicarFiltro").addEventListener("click", () => {
  const tipo = document.getElementById("tipoFiltro").value;
  const valor = document.getElementById("valorFiltro").value.trim();

  if (!valor) {
    return showModal({
      title: "Atenção",
      message: "Digite um ID para filtrar.",
      type: "warning"
    });
  }

  let filtrados = [];

  if (tipo === "item") {
    filtrados = itensOriginais.filter(i => i.id == valor);
  }

  if (filtrados.length === 0) {
    showModal({
      title: "Nenhum resultado",
      message: "Nenhum item encontrado com esse ID.",
      type: "info"
    });
  }

  todosItens = filtrados;
  paginaAtual = 1;
  renderizarTabela();
});


// ==============================================
// 11. LIMPAR FILTRO E RESTAURAR LISTA
// ==============================================

document.getElementById("btnLimparFiltro").addEventListener("click", () => {
  todosItens = [...itensOriginais];
  paginaAtual = 1;
  renderizarTabela();
  document.getElementById("valorFiltro").value = "";
});