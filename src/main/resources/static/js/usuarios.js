import { chamarApi } from "./api.js";
import { escaparHtml, executar, mostrarMensagem } from "./interface.js";

const formulario = document.querySelector("#formUsuario");
const lista = document.querySelector("#listaUsuarios");

async function atualizarTela() {
    const usuarios = await chamarApi("/usuarios");
    lista.innerHTML = usuarios.length ? usuarios.map(usuario => `
        <div class="item-usuario">
            <span><strong>${escaparHtml(usuario.nome)}</strong> — ${escaparHtml(usuario.email)}</span>
            <button type="button" class="botao-pequeno botao-perigo" data-excluir-usuario="${usuario.id}">Excluir</button>
        </div>
    `).join("") : '<p class="vazio">Nenhum usuário cadastrado.</p>';
}

formulario.addEventListener("submit", evento => {
    evento.preventDefault();
    executar(async () => {
        const dados = new FormData(formulario);
        await chamarApi("/usuarios", "POST", {
            nome: dados.get("nome"),
            email: dados.get("email"),
            idade: Number(dados.get("idade"))
        });
        formulario.reset();
        await atualizarTela();
        mostrarMensagem("Usuário cadastrado com sucesso.");
    });
});

lista.addEventListener("click", evento => {
    const botao = evento.target.closest("button[data-excluir-usuario]");
    if (!botao || !confirm("Confirma esta exclusão?")) return;
    executar(async () => {
        await chamarApi(`/usuarios/${botao.dataset.excluirUsuario}`, "DELETE");
        await atualizarTela();
        mostrarMensagem("Operação concluída.");
    });
});

document.querySelector("#atualizar").addEventListener("click", () => executar(async () => {
    await atualizarTela();
    mostrarMensagem("Dados atualizados.");
}));
executar(atualizarTela);
