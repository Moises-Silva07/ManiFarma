document.addEventListener("DOMContentLoaded", async () => {
  const clienteId = localStorage.getItem("userId");
  const tabela = document.getElementById("tabelaPedidos");
  const msg = document.getElementById("message");

  if (!clienteId) {
    msg.textContent = "Erro: cliente não identificado. Faça login novamente.";
    msg.style.color = "red";
    return;
  }

  const { ok, data } = await apiRequest(`/api/pedidos/cliente/${clienteId}`, "GET", null, true, true);

  if (ok && Array.isArray(data) && data.length > 0) {
    msg.textContent = "";
    tabela.innerHTML = "";

    data.forEach(pedido => {
      const row = document.createElement("tr");

      const receitaCell = pedido.receita
        ? `
           ${pedido.receita} 
           <button class="btn btn-outline-primary btn-sm ver-receita" data-id="${pedido.id}">
             📄 Ver Receita
           </button>`
        : "Nenhuma";

      row.innerHTML = `
        <td>${pedido.id}</td>
        <td>${pedido.descricao}</td>
        <td>${pedido.status || "Pendente"}</td>
        <td>${receitaCell}</td>
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
          // Usa URL completa (garante compatibilidade mesmo fora do localhost)
          const response = await fetch(`http://localhost:8080/api/pedidos/${pedidoId}/receita`);

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

  } else if (ok && data.length === 0) {
    msg.textContent = "Você ainda não possui pedidos.";
    msg.style.color = "gray";
  } else {
    msg.textContent = "Erro ao carregar seus pedidos. Tente novamente mais tarde.";
    msg.style.color = "red";
  }
});
