import { chamarApi } from "./api.js";
import { executar, mostrarMensagem } from "./interface.js";
import { configurarAcoesEventos, mostrarEventos } from "./lista-eventos.js";

const formulario = document.querySelector("#formEvento");
const tituloFormulario = document.querySelector("#tituloFormulario");
const botaoSalvar = document.querySelector("#salvarEvento");
const botaoCancelar = document.querySelector("#cancelarEdicao");
let eventoEmEdicao = null;

function cancelarEdicao() {
    eventoEmEdicao = null;
    formulario.reset();
    tituloFormulario.textContent = "Cadastrar evento";
    botaoSalvar.textContent = "Cadastrar evento";
    botaoCancelar.hidden = true;
}

async function editarEvento(id) {
    const evento = await chamarApi(`/eventos/${id}`);
    eventoEmEdicao = evento.id;
    formulario.elements.titulo.value = evento.titulo;
    formulario.elements.descricao.value = evento.descricao || "";
    formulario.elements.data.value = evento.data;
    formulario.elements.local.value = evento.local;
    formulario.elements.capacidade.value = evento.capacidade;
    tituloFormulario.textContent = "Editar evento";
    botaoSalvar.textContent = "Salvar alterações";
    botaoCancelar.hidden = false;
    formulario.elements.titulo.focus();
}

async function atualizarTela() {
    await mostrarEventos(await chamarApi("/eventos"), true);
}

formulario.addEventListener("submit", evento => {
    evento.preventDefault();
    executar(async () => {
        const dados = new FormData(formulario);
        const editando = eventoEmEdicao !== null;
        const caminho = editando ? `/eventos/${eventoEmEdicao}` : "/eventos";
        await chamarApi(caminho, editando ? "PUT" : "POST", {
            titulo: dados.get("titulo"),
            descricao: dados.get("descricao"),
            data: dados.get("data"),
            local: dados.get("local"),
            capacidade: Number(dados.get("capacidade"))
        });
        cancelarEdicao();
        await atualizarTela();
        mostrarMensagem(editando ? "Evento atualizado com sucesso." : "Evento cadastrado com sucesso.");
    });
});

configurarAcoesEventos(atualizarTela, {
    editarEvento,
    aoExcluirEvento: id => {
        if (String(eventoEmEdicao) === id) cancelarEdicao();
    }
});
botaoCancelar.addEventListener("click", cancelarEdicao);
document.querySelector("#atualizar").addEventListener("click", () => executar(async () => {
    await atualizarTela();
    mostrarMensagem("Dados atualizados.");
}));
executar(atualizarTela);
