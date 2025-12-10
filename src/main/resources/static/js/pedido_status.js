// Verifica se o usuário está logado com Token
validarToken();

document.addEventListener("DOMContentLoaded", async () => {
  const clienteId = localStorage.getItem("userId");
  const tabela = document.getElementById("tabelaPedidos");
  const msg = document.getElementById("message");

  if (!clienteId) {
    showModal({
            title: "Atenção",
            message: "Usuário não identificado. Faça login novamente.",
            type: "warning",
        });
    window.location.href = "/html/login/login.html";
  }

  const statusFormatado = {
    PENDENTE: "Pendente",
    VALIDO: "Válido",
    ENVIODECOTACAO: "Envio de Cotação",
    PAGO: "Pago",
    CONCLUIDO: "Concluído",
    CANCELADO: "Cancelado"
  };

  function formatarStatus(status) {
    return statusFormatado[status] || status;
  }


  const { ok, data } = await apiRequest(`/api/pedidos/cliente/${clienteId}`, "GET", null, true, true);

  if (ok && Array.isArray(data) && data.length > 0) {
    msg.textContent = "";
    tabela.innerHTML = "";

    data.forEach(pedido => {
      const row = document.createElement("tr");

      const receitaCell = pedido.receita
        ? `
            <div class="receita-box">
              <span class="receita-nome">${pedido.receita}</span>
              <button class="btn btn-outline-primary btn-sm ver-receita" data-id="${pedido.id}">
                Ver Receita
              </button>
            </div>
          `
        : `<span class="text-muted">Nenhuma</span>`;

        const pagamentoCell = (() => {
          if (pedido.status === "CANCELADO") {
            return `<span class="text-danger fw-bold">Cotação cancelada</span>`;
          }

          if (pedido.linkPagamento) {
            return `
              <a href="${pedido.linkPagamento}" target="_blank" class="btn btn-success btn-sm">
                💳 Pagar Agora
              </a>`;
          }

          if (pedido.status === "VALIDO") {
            return `<span class="text-primary fw-bold">Cotação gerada, aguardando pagamento</span>`;
          }

          return "Aguardando cotação";
        })();

      row.innerHTML = `
        <td>${pedido.id}</td>
        <td>${pedido.descricao}</td>
        <td>${formatarStatus(pedido.status) || "Pendente"}</td>
        <td class="receita-cell">${receitaCell}</td>
        <td>${pagamentoCell}</td>
      `;

      tabela.appendChild(row);
    });

    // Evento para visualizar receita (com correção do target)
    document.querySelectorAll(".ver-receita").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        // Usa currentTarget (garante o elemento <button>, não o emoji ou texto dentro dele)
        const pedidoId = e.currentTarget.getAttribute("data-id");

        const img = document.getElementById("imagemReceita");
        const msgErro = document.getElementById("mensagemErroReceita");
        const modal = new bootstrap.Modal(document.getElementById("modalReceita"));

        // Limpa estado anterior
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
          showModal({
            title: "Erro",
            message: "Erro ao buscar imagem do servidor.",
            type: "danger",
        });
          msgErro.classList.remove("d-none");
          modal.show();
        }
      });
    });

  } else if (ok && data.length === 0) {
    showModal({
            title: "Atenção",
            message: "Você ainda não possui pedidos.",
            type: "warning",
        });
  } else {
    showModal({
            title: "Erro",
            message: "Erro ao carregar seus pedidos. Tente novamente mais tarde.",
            type: "danger",
        });
  }
});

// ============================
//  VARIÁVEIS DE CONTEXTO
// ============================
let pedidoEmValidacao = null;
let statusEmValidacao = null;

// Bootstrap Modal (será inicializado quando precisar)
function getModalSenhaInstance() {
  const modalEl = document.getElementById('modalSenhaFarmaceutico');
  return bootstrap.Modal.getOrCreateInstance(modalEl);
}

// ============================
//  FUNÇÃO PÚBLICA: ALTERAR STATUS
// ============================
// Chame ESTA função em vez de ir direto pro fetch
// exemplo: alterarStatusPedido(pedido.id, 'VALIDO');
async function alterarStatusPedido(pedidoId, novoStatus) {
  // Se não for VALIDAR, vai direto pra API sem senha
  if (novoStatus !== 'VALIDO') {
    return enviarStatusParaAPI(pedidoId, novoStatus, null);
  }

  // Se for VALIDAR, abre o modal de senha
  pedidoEmValidacao = pedidoId;
  statusEmValidacao = novoStatus;

  const inputSenha = document.getElementById('senhaFarmaceutico');
  const erroSenha = document.getElementById('erroSenhaFarmaceutico');
  inputSenha.value = '';
  erroSenha.classList.add('d-none');
  erroSenha.textContent = '';

  const modal = getModalSenhaInstance();
  modal.show();
}

// ============================
//  clique no botão CONFIRMAR do modal
// ============================
document.getElementById('btnConfirmarSenhaFarmaceutico')
  .addEventListener('click', async () => {
    const inputSenha = document.getElementById('senhaFarmaceutico');
    const erroSenha = document.getElementById('erroSenhaFarmaceutico');
    const senha = inputSenha.value.trim();

    if (!senha) {
      erroSenha.textContent = 'Informe a senha do farmacêutico.';
      erroSenha.classList.remove('d-none');
      return;
    }

    try {
      await enviarStatusParaAPI(pedidoEmValidacao, statusEmValidacao, senha);
      getModalSenhaInstance().hide();
    } catch (e) {
      // erro já tratado dentro de enviarStatusParaAPI
    }
  });


// ============================
//  FUNÇÃO QUE REALMENTE CHAMA A API
// ============================
async function enviarStatusParaAPI(pedidoId, status, senha) {
  try {
    const body = { status: status }; // backend espera "status"
    if (senha) {
      body.senha = senha; // backend espera "senha"
    }

    const token = localStorage.getItem('token'); // ajuste se você guarda em outro lugar

    const res = await fetch(`/api/pedidos/${pedidoId}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body: JSON.stringify(body)
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
      // 403 = senha errada / não enviada
      if (res.status === 403) {
        mostrarToast('Erro de autorização', data.error || 'Senha do farmacêutico incorreta.', true);

        const erroSenha = document.getElementById('erroSenhaFarmaceutico');
        if (erroSenha) {
          erroSenha.textContent = data.error || 'Senha incorreta.';
          erroSenha.classList.remove('d-none');
        }
        throw new Error(data.error || 'Erro de autorização');
      }

      mostrarToast('Erro', data.error || 'Erro ao atualizar status.', true);
      throw new Error(data.error || 'Erro ao atualizar status');
    }

    mostrarToast('Sucesso', data.message || 'Status atualizado com sucesso!');
    // Recarrega a lista de pedidos ou atualiza linha
    if (typeof carregarPedidos === 'function') {
      carregarPedidos();
    }
  } catch (err) {
    console.error(err);
    if (!err.manual) {
      mostrarToast('Erro', 'Falha de comunicação com o servidor.', true);
    }
    throw err;
  }
}


// ============================
//  helper de toast simples
//  (usa o toast que você já tem na página)
// ============================
function mostrarToast(titulo, mensagem, isErro = false) {
  const toastEl = document.getElementById('mainToast');
  const toastBody = document.getElementById('toastBody');

  if (!toastEl || !toastBody) {
    alert(mensagem); // fallback
    return;
  }

  toastBody.innerHTML = `<strong>${titulo}:</strong> ${mensagem}`;
  toastEl.classList.remove('text-bg-success', 'text-bg-danger');
  toastEl.classList.add(isErro ? 'text-bg-danger' : 'text-bg-success');

  const toast = bootstrap.Toast.getOrCreateInstance(toastEl);
  toast.show();
}
