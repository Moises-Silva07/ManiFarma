function validarCEP(cep) {
    return /^[0-9]{8}$/.test(cep.replace(/\D/g, ""));
}

function limpaCEP() {
    ["rua","bairro","cidade","uf"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = "";
    });
}

function preencherCEP(data) {
    if (!data || data.erro) {
        limpaCEP();
        showModal({
            title: "CEP inválido",
            message: "Não foi possível localizar o CEP informado.",
            type: "warning",
        });
        return;
    }

    document.getElementById("rua").value = data.logradouro;
    document.getElementById("bairro").value = data.bairro;
    document.getElementById("cidade").value = data.localidade;
    document.getElementById("uf").value = data.uf;
}

function buscarCEP(valor) {
    const cep = valor.replace(/\D/g, "");

    if (cep.length !== 8) {
        limpaCEP();
        return;
    }

    document.getElementById("rua").value = "...";
    document.getElementById("bairro").value = "...";
    document.getElementById("cidade").value = "...";
    document.getElementById("uf").value = "...";

    const script = document.createElement("script");
    script.src = `https://viacep.com.br/ws/${cep}/json/?callback=preencherCEP`;
    document.body.appendChild(script);
}
