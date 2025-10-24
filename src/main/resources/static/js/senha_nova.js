const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get("token");

    document.getElementById("novaSenhaForm").addEventListener("submit", async (e) => {

    e.preventDefault();

    const novaSenha = document.getElementById("novaSenha").value;
    const confirmarSenha = document.getElementById("confirmarNovaSenha").value;

    if (novaSenha !== confirmarSenha) {
      document.getElementById("mensagem").innerText = "As senhas não conferem!";
      return;
    }

    const resposta = await apiRequest("/login/senha_nova", "POST", { token, novaSenha });

    if (resposta.ok) {
      document.getElementById("mensagem").innerText = resposta.data.mensagem || "Senha alterada com sucesso!";
      
    } else {
      document.getElementById("mensagem").innerText = resposta.data.mensagem || "Erro ao redefinir senha.";
    }
  });