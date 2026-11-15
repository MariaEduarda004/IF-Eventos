import { chamarApi } from "./api.js";
import { executar, mostrarMensagem } from "./interface.js";
import { configurarAcoesEventos, mostrarEventos } from "./lista-eventos.js";

const formulario = document.querySelector("#formEvento");

async function atualizarTela() {
    await mostrarEventos(await chamarApi("/eventos"));
}

formulario.addEventListener("submit", evento => {
    evento.preventDefault();
    executar(async () => {
        const dados = new FormData(formulario);
        await chamarApi("/eventos", "POST", {
            titulo: dados.get("titulo"),
            descricao: dados.get("descricao"),
            data: dados.get("data"),
            local: dados.get("local"),
            capacidade: Number(dados.get("capacidade"))
        });
        formulario.reset();
        await atualizarTela();
        mostrarMensagem("Evento cadastrado com sucesso.");
    });
});

configurarAcoesEventos(atualizarTela);
document.querySelector("#atualizar").addEventListener("click", () => executar(async () => {
    await atualizarTela();
    mostrarMensagem("Dados atualizados.");
}));
executar(atualizarTela);
