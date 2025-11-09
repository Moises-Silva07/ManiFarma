// /js/itens_funcionario.js
document.addEventListener("DOMContentLoaded", carregarItens);

async function carregarItens() {
  const tabela = document.getElementById("corpoItens");
  const msg = document.getElementById("message");

  const { ok, data } = await apiRequest("/produtos", "GET");

  if (ok && Array.isArray(data) && data.length > 0) {
    msg.textContent = "";
    tabela.innerHTML = "";

    data.forEach(item => {
      const linha = document.createElement("tr");
      linha.dataset.id = item.id; // guarda o ID na linha
      linha.innerHTML = `
        <td>${item.id}</td>
        <td>${item.nome}</td>
        <td>R$ ${item.preco.toFixed(2).replace('.', ',')}</td>
      `;
      linha.addEventListener("click", () => selecionarLinha(linha));
      tabela.appendChild(linha);
    });
  } else if (ok && data.length === 0) {
    msg.textContent = "Nenhum item cadastrado.";
    msg.style.color = "gray";
  } else {
    msg.textContent = "Erro ao carregar os itens.";
    msg.style.color = "red";
  }
}

let linhaSelecionada = null;

function selecionarLinha(linha) {
  if (linhaSelecionada) linhaSelecionada.classList.remove("selecionada");
  linhaSelecionada = linha;
  linha.classList.add("selecionada");
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
