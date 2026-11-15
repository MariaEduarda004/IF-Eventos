import { chamarApi } from "./api.js";
import { escaparHtml, executar, mostrarMensagem } from "./interface.js";

const formulario = document.querySelector("#formUsuario");
const lista = document.querySelector("#listaUsuarios");
const tituloFormulario = document.querySelector("#tituloFormulario");
const botaoSalvar = document.querySelector("#salvarUsuario");
const botaoCancelar = document.querySelector("#cancelarEdicao");
let usuarioEmEdicao = null;

function cancelarEdicao() {
    usuarioEmEdicao = null;
    formulario.reset();
    tituloFormulario.textContent = "Cadastrar usuário";
    botaoSalvar.textContent = "Cadastrar usuário";
    botaoCancelar.hidden = true;
}

async function editarUsuario(id) {
    const usuario = await chamarApi(`/usuarios/${id}`);
    usuarioEmEdicao = usuario.id;
    formulario.elements.nome.value = usuario.nome;
    formulario.elements.email.value = usuario.email;
    formulario.elements.idade.value = usuario.idade;
    tituloFormulario.textContent = "Editar usuário";
    botaoSalvar.textContent = "Salvar alterações";
    botaoCancelar.hidden = false;
    formulario.elements.nome.focus();
}

async function atualizarTela() {
    const usuarios = await chamarApi("/usuarios");
    lista.innerHTML = usuarios.length ? usuarios.map(usuario => `
        <div class="item-usuario">
            <span><strong>${escaparHtml(usuario.nome)}</strong> — ${escaparHtml(usuario.email)}</span>
            <div class="acoes">
                <button type="button" class="botao-pequeno botao-secundario" data-editar-usuario="${usuario.id}">Editar</button>
                <button type="button" class="botao-pequeno botao-perigo" data-excluir-usuario="${usuario.id}">Excluir</button>
            </div>
        </div>
    `).join("") : '<p class="vazio">Nenhum usuário cadastrado.</p>';
}

formulario.addEventListener("submit", evento => {
    evento.preventDefault();
    executar(async () => {
        const dados = new FormData(formulario);
        const editando = usuarioEmEdicao !== null;
        const caminho = editando ? `/usuarios/${usuarioEmEdicao}` : "/usuarios";
        await chamarApi(caminho, editando ? "PUT" : "POST", {
            nome: dados.get("nome"),
            email: dados.get("email"),
            idade: Number(dados.get("idade"))
        });
        cancelarEdicao();
        await atualizarTela();
        mostrarMensagem(editando ? "Usuário atualizado com sucesso." : "Usuário cadastrado com sucesso.");
    });
});

lista.addEventListener("click", evento => {
    const botao = evento.target.closest("button");
    if (!botao) return;
    executar(async () => {
        if (botao.dataset.editarUsuario) {
            await editarUsuario(botao.dataset.editarUsuario);
        } else if (botao.dataset.excluirUsuario && confirm("Confirma esta exclusão?")) {
            await chamarApi(`/usuarios/${botao.dataset.excluirUsuario}`, "DELETE");
            if (String(usuarioEmEdicao) === botao.dataset.excluirUsuario) cancelarEdicao();
            await atualizarTela();
            mostrarMensagem("Operação concluída.");
        }
    });
});

botaoCancelar.addEventListener("click", cancelarEdicao);
document.querySelector("#atualizar").addEventListener("click", () => executar(async () => {
    await atualizarTela();
    mostrarMensagem("Dados atualizados.");
}));
executar(atualizarTela);
