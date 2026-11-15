import { chamarApi } from "./api.js";
import { executar, mostrarMensagem, preencherSelecao } from "./interface.js";
import { configurarAcoesEventos, mostrarEventos } from "./lista-eventos.js";

const formulario = document.querySelector("#formInscricao");
const usuario = document.querySelector("#usuarioId");
const evento = document.querySelector("#eventoId");

async function atualizarTela() {
    const [usuarios, eventos] = await Promise.all([chamarApi("/usuarios"), chamarApi("/eventos")]);
    preencherSelecao(usuario, usuarios, item => item.nome);
    preencherSelecao(evento, eventos, item => item.titulo);
    await mostrarEventos(eventos);
}

formulario.addEventListener("submit", acao => {
    acao.preventDefault();
    executar(async () => {
        await chamarApi("/inscricoes", "POST", {
            usuarioId: Number(usuario.value),
            eventoId: Number(evento.value)
        });
        await atualizarTela();
        mostrarMensagem("Inscrição realizada com sucesso.");
    });
});

configurarAcoesEventos(atualizarTela);
document.querySelector("#atualizar").addEventListener("click", () => executar(async () => {
    await atualizarTela();
    mostrarMensagem("Dados atualizados.");
}));
executar(atualizarTela);
