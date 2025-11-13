// Função genérica para modais (alertas e confirmações)
function showModal({
  title = "Mensagem",
  message = "",
  confirmText = "OK",
  cancelText = null,
  type = "info" // success | danger | warning | info | confirm
}) {
  return new Promise((resolve) => {
    // Caso seja um modal de confirmação
    if (cancelText || type === "confirm") {
      const modalEl = document.getElementById("modalConfirm");
      const modalBody = document.getElementById("modalConfirmMessage");
      const modalTitle = modalEl.querySelector(".modal-title");
      const confirmBtn = document.getElementById("modalConfirmYes");

      modalTitle.textContent = title;
      modalBody.textContent = message;

      const modal = new bootstrap.Modal(modalEl);
      modal.show();

      confirmBtn.onclick = () => {
        modal.hide();
        resolve(true);
      };

      modalEl.addEventListener(
        "hidden.bs.modal",
        () => resolve(false),
        { once: true }
      );

      return;
    }

    // Caso contrário, modal simples de alerta
    const modalEl = document.getElementById("modalAlert");
    const modalTitle = document.getElementById("modalAlertTitle");
    const modalBody = document.getElementById("modalAlertMessage");
    const header = modalEl.querySelector(".modal-header");

    modalTitle.textContent = title;
    modalBody.textContent = message;

    // Define cor do cabeçalho conforme o tipo
    header.className = "modal-header text-white";
    switch (type) {
      case "success":
        header.classList.add("bg-success");
        break;
      case "danger":
        header.classList.add("bg-danger");
        break;
      case "warning":
        header.classList.add("bg-warning", "text-dark");
        break;
      default:
        header.classList.add("bg-primary");
    }

    new bootstrap.Modal(modalEl).show();
    resolve(true);
  });
}

// Função global para exibir toasts (mensagens rápidas)
function showToast(message, type = "info") {
  const toastEl = document.getElementById("mainToast");
  const toastBody = document.getElementById("toastBody");

  toastBody.textContent = message;

  // Define a cor de fundo conforme o tipo
  toastEl.className = "toast align-items-center border-0 text-white";
  switch (type) {
    case "success":
      toastEl.classList.add("bg-success");
      break;
    case "danger":
      toastEl.classList.add("bg-danger");
      break;
    case "warning":
      toastEl.classList.add("bg-warning", "text-dark");
      break;
    default:
      toastEl.classList.add("bg-primary");
  }

  // Exibe o toast com delay de 3 segundos
  const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
  toast.show();
}
