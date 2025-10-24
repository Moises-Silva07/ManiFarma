document.getElementById("form").addEventListener("submit", async (e) => {
    
    e.preventDefault();


    const nome = document.getElementById("nome").value.trim();
    const email = document.getElementById("email").value.trim();
    const cpf = document.getElementById("cpf").value.trim();
    const endereco = document.getElementById("endereco").value.trim();
    const telefone = document.getElementById("telefone").value.trim();
    const senha = document.getElementById("senha").value;
    const confirmaSenha = document.getElementById("confirmaSenha").value;


    console.log("Valor do campo 'senha':", `"${senha}"`);
    console.log("Valor do campo 'confirmaSenha':", `"${confirmaSenha}"`);


    if (senha !== confirmaSenha) {
        document.getElementById("message").textContent = "As senhas não conferem!";
        return; 
    }


    const endpoint = "/api/auth/register";


    const requestBody = {
        nome: nome,
        email: email,
        senha: senha,
        cpf: cpf,
        endereco: endereco,
        telefone: telefone,
        client: true
    };



    const {ok, data} = await apiRequest(endpoint, "POST", requestBody);


    if (ok) {
        document.getElementById("message").textContent = "Cadastro realizado com sucesso! Redirecionando para o login...";
        setTimeout(() => { 
            window.location.href = "/html/login/login.html"; 
        }, 2000);
    } else {
        document.getElementById("message").textContent = data?.mensagem || "Cadastro inválido! Verifique os dados.";
    }
});
