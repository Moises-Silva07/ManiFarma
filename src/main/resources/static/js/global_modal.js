// Função para exibir alertas com modal Bootstrap
function showAlert(message, type = "primary") {
  const modal = document.getElementById("modalAlert");
  const title = document.getElementById("modalAlertTitle");
  const body = document.getElementById("modalAlertMessage");

  title.className = "modal-title text-" + type;
  body.textContent = message;

  const bsModal = new bootstrap.Modal(modal);
  bsModal.show();
}

// Função para confirmação (tipo confirm)
function showConfirm(message, onConfirm) {
  const modal = document.getElementById("modalConfirm");
  const msg = document.getElementById("modalConfirmMessage");
  const btnYes = document.getElementById("modalConfirmYes");

  msg.textContent = message;

  // Remove event listeners antigos
  const newBtn = btnYes.cloneNode(true);
  btnYes.parentNode.replaceChild(newBtn, btnYes);

  newBtn.addEventListener("click", () => {
    const bsModal = bootstrap.Modal.getInstance(modal);
    bsModal.hide();
    if (typeof onConfirm === "function") onConfirm();
  });

  const bsModal = new bootstrap.Modal(modal);
  bsModal.show();
}

// Função para exibir toast (notificação leve)
function showToast(message, type = "success") {
  const toast = document.getElementById("mainToast");
  const body = document.getElementById("toastBody");

  body.textContent = message;

  toast.className = `toast align-items-center text-bg-${type} border-0`;

  const bsToast = new bootstrap.Toast(toast);
  bsToast.show();
}
