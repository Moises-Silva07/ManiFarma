document.getElementById("recuperaForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;

    const {ok, data} = await apiRequest(
        "/login/recupera_senha",
        "POST",
        {email});

    if (ok) {
        localStorage.setItem("token", data.token); 
        document.getElementById("mensagem").textContent = "Verifique seu e-mail para redefinir a senha."
    } else {
        document.getElementById("mensagem").textContent = "Erro ao solicitar recuperação de senha."
    }
 
})