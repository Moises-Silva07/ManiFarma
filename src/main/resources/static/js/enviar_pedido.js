// Verifica se o usuário está logado
const userId = localStorage.getItem("userId");
if (!userId) {
    alert("Usuário não identificado. Faça login novamente.");
    window.location.href = "/html/login/login.html";
}

document.getElementById("form").addEventListener("submit", async (e) => {
  e.preventDefault();

  const descricao = document.getElementById("descricao").value.trim();
  const receitaInput = document.getElementById("receita");
  const arquivo = receitaInput.files[0];

  if (!descricao || !arquivo) {
    alert("Por favor, preencha a descrição e selecione um arquivo de receita.");
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
      alert("Pedido enviado com sucesso!");
      document.getElementById("form").reset();
      console.log("Resposta do servidor:", data);
    } else {
      console.error("Erro:", data);
      alert("Erro ao enviar pedido: " + (data.error || "Desconhecido"));
    }
  } catch (err) {
    console.error("Erro geral:", err);
    alert("Erro inesperado ao enviar pedido.");
  }
});

