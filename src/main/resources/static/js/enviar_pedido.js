// Verifica se o usuário está logado com Token
validarToken(); 

// Verifica se o usuário está logado por ID
const userId = localStorage.getItem("userId");
if (!userId) {
    showModal({
            title: "Atenção",
            message: "Usuário não identificado. Faça login novamente.",
            type: "warning",
        });
    window.location.href = "/html/login/login.html";
}

document.getElementById("form").addEventListener("submit", async (e) => {
  e.preventDefault();

  const descricao = document.getElementById("descricao").value.trim();
  const receitaInput = document.getElementById("receita");
  const arquivo = receitaInput.files[0];

  if (!descricao || !arquivo) {
    showModal({
            title: "Atenção",
            message: "Por favor, preencha a descrição e selecione um arquivo de receita.",
            type: "warning",
        });
    return;
  }

  const clienteId = parseInt(localStorage.getItem("userId"));

  const formData = new FormData();
  formData.append("descricao", descricao);
  formData.append("clienteId", clienteId);
  formData.append("receita", arquivo);

  console.log("FormData enviado:");
  for (let [key, value] of formData.entries()) {
    console.log(`${key}:`, value);
  }

  try {
    const resposta = await fetch("http://localhost:8080/api/pedidos", {
      method: "POST",
      body: formData, // não define headers, o browser faz isso automaticamente
    });

    const data = await resposta.json();

    if (resposta.ok) {
      showModal({
            title: "Sucesso!",
            message: "Pedido enviado com sucesso!" + " " + `Acompanhe os seus pedidos em "Meus Pedidos". `,
            type: "success"
        });

      document.getElementById("form").reset();
      console.log("Resposta do servidor:", data);

    } else {
      console.error("Erro:", data);
      showModal({
            title: "Erro",
            message: "Erro ao enviar pedido.",
            type: "danger",
        });
    }
  } catch (err) {
    console.error("Erro geral:", err);
    showModal({
            title: "Erro",
            message: "Erro inesperado ao enviar pedido.",
            type: "danger",
        });
  }
});

