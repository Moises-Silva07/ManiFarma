// ======================================================================
//  AUTENTICAÇÃO
// ======================================================================
validarToken();

let pedidoIdAlvo = null;
let novoStatusAlvo = null;
let modalSenhaInstance = null;

// ======================================================================
// CARREGAR PEDIDOS DO FUNCIONÁRIO
// ======================================================================
document.addEventListener("DOMContentLoaded", async () => {
    await carregarPedidos();
});

// ======================================================================
async function carregarPedidos() {
    const tabela = document.getElementById("corpoTabela");
    const msg = document.getElementById("message");

    const { ok, data } = await apiRequest("/api/pedidos/status/PENDENTE", "GET", null, true, true);

    if (!ok) {
        tabela.innerHTML = `<tr><td colspan="5" class="text-danger text-center">Erro ao carregar pedidos.</td></tr>`;
        return;
    }

    tabela.innerHTML = "";

    if (data.length === 0) {
        tabela.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Nenhum pedido pendente.</td></tr>`;
        return;
    }

    data.forEach(pedido => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${pedido.id}</td>
            <td>${pedido.employeeId ?? "—"}</td>
            <td>${pedido.status}</td>
            <td>R$ ${(pedido.valorTotal ?? 0).toFixed(2)}</td>
            <td>
                <button class="btn btn-success btn-sm" onclick="solicitarAlteracaoStatus(${pedido.id}, 'VALIDO')">Validar</button>
                <button class="btn btn-danger btn-sm" onclick="solicitarAlteracaoStatus(${pedido.id}, 'CANCELADO')">Cancelar</button>
                <button class="btn btn-primary btn-sm ver-receita" data-id="${pedido.id}">Receita</button>
            </td>
        `;

        tabela.appendChild(tr);
    });

    prepararEventosReceita();
}

// ======================================================================
// EVENTO PARA VISUALIZAR RECEITA
// ======================================================================
function prepararEventosReceita() {
    document.querySelectorAll(".ver-receita").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const pedidoId = e.currentTarget.getAttribute("data-id");

            const img = document.getElementById("imagemReceita");
            const msgErro = document.getElementById("mensagemErroReceita");
            const modal = new bootstrap.Modal(document.getElementById("modalReceita"));

            img.src = "";
            msgErro.classList.add("d-none");

            try {
                const token = localStorage.getItem("token");

                const response = await fetch(`/api/pedidos/${pedidoId}/receita`, {
                    method: "GET",
                    headers: { "Authorization": `Bearer ${token}` }
                });

                if (!response.ok) {
                    msgErro.textContent = "Erro ao carregar imagem.";
                    msgErro.classList.remove("d-none");
                } else {
                    const blob = await response.blob();
                    img.src = URL.createObjectURL(blob);
                }

                modal.show();

            } catch (error) {
                msgErro.textContent = "Erro inesperado ao buscar imagem.";
                msgErro.classList.remove("d-none");
                modal.show();
            }
        });
    });
}

// ======================================================================
// SOLICITAR ALTERAÇÃO DE STATUS (VERIFICA SE PRECISA DE SENHA)
// ======================================================================
function solicitarAlteracaoStatus(pedidoId, novoStatus) {
    pedidoIdAlvo = pedidoId;
    novoStatusAlvo = novoStatus.toUpperCase();

    if (novoStatusAlvo === "VALIDO") {
        document.getElementById("inputSenhaFarmaceutico").value = "";
        document.getElementById("erroSenhaFarmaceutico").classList.add("d-none");

        modalSenhaInstance = bootstrap.Modal.getOrCreateInstance(
            document.getElementById("modalSenhaFarmaceutico")
        );
        modalSenhaInstance.show();
    } else {
        enviarAlteracaoStatusComSenha(null);
    }
}

// ======================================================================
// CONFIRMAR SENHA DO MODAL
// ======================================================================
document.getElementById("btnConfirmarSenhaFarmaceutico").addEventListener("click", () => {
    const senha = document.getElementById("inputSenhaFarmaceutico").value.trim();
    const erro = document.getElementById("erroSenhaFarmaceutico");

    if (!senha) {
        erro.textContent = "Informe a senha.";
        erro.classList.remove("d-none");
        return;
    }

    enviarAlteracaoStatusComSenha(senha);
});

// ======================================================================
// REALIZA A ALTERAÇÃO DE STATUS NO BACKEND
// ======================================================================
async function enviarAlteracaoStatusComSenha(senha) {
    if (!pedidoIdAlvo || !novoStatusAlvo) {
        alert("Erro: Pedido ou status não definido.");
        return;
    }

    try {
        const token = localStorage.getItem("token");

        const response = await fetch(`/api/pedidos/${pedidoIdAlvo}/status`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                status: novoStatusAlvo,
                senha: senha
            })
        });

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            showModal({
                title: "Erro",
                message: data.error || "Erro ao atualizar status",
                type: "danger"
            });
            return;
        }

        if (modalSenhaInstance) modalSenhaInstance.hide();

        showModal({
            title: "Sucesso!",
            message: "Status atualizado com sucesso!",
            type: "success"
        });

        await carregarPedidos();

    } catch (error) {
        showModal({
            title: "Erro",
            message: "Erro inesperado: " + error.message,
            type: "danger"
        });
    }
}
