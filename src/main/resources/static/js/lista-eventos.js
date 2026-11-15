import { baixarParticipantes, chamarApi } from "./api.js";
import { escaparHtml, executar, mostrarMensagem } from "./interface.js";

function cartaoEvento(evento, inscricoes, permiteEdicao) {
    const participantes = inscricoes.map(inscricao => `
        <div class="item-inscricao">
            <span>${escaparHtml(inscricao.usuarioNome)}</span>
            <button type="button" class="botao-pequeno botao-perigo" data-cancelar-inscricao="${inscricao.id}">Cancelar</button>
        </div>
    `).join("");
    const percentual = Math.min(100, (evento.inscritos / evento.capacidade) * 100);
    const vagas = evento.vagasDisponiveis;

    return `
        <article class="cartao">
            <h3>${escaparHtml(evento.titulo)}</h3>
            <p>${escaparHtml(evento.descricao || "Sem descrição")}</p>
            <p><strong>Data:</strong> ${escaparHtml(evento.data)}</p>
            <p><strong>Local:</strong> ${escaparHtml(evento.local)}</p>
            <p><strong>Inscritos:</strong> ${evento.inscritos} / ${evento.capacidade}</p>
            <div class="barra"><div class="progresso" style="width: ${percentual}%"></div></div>
            <p class="${vagas === 0 ? "aviso" : ""}">${vagas === 0 ? "Vagas esgotadas" : `${vagas} vaga(s) disponível(is)`}</p>
            <div class="subtitulo">Participantes</div>
            <div class="acoes">
                <button type="button" class="botao-pequeno" data-exportar="json" data-evento-id="${evento.id}">Exportar JSON</button>
                <button type="button" class="botao-pequeno" data-exportar="xml" data-evento-id="${evento.id}">Exportar XML</button>
            </div>
            ${participantes || '<p class="vazio">Sem inscritos.</p>'}
            <div class="acoes">
                ${permiteEdicao ? `<button type="button" class="botao-pequeno botao-secundario" data-editar-evento="${evento.id}">Editar evento</button>` : ""}
                <button type="button" class="botao-pequeno botao-perigo" data-excluir-evento="${evento.id}">Excluir evento</button>
            </div>
        </article>
    `;
}

export async function mostrarEventos(eventos, permiteEdicao = false) {
    const lista = document.querySelector("#listaEventos");
    if (eventos.length === 0) {
        lista.innerHTML = '<p class="vazio">Nenhum evento cadastrado.</p>';
        return;
    }
    const inscricoes = await Promise.all(
        eventos.map(evento => chamarApi(`/inscricoes/evento/${evento.id}`))
    );
    lista.innerHTML = eventos.map((evento, indice) => cartaoEvento(evento, inscricoes[indice], permiteEdicao)).join("");
}

export function configurarAcoesEventos(atualizarTela, { editarEvento, aoExcluirEvento } = {}) {
    document.querySelector("#listaEventos").addEventListener("click", evento => {
        const botao = evento.target.closest("button");
        if (!botao) return;

        executar(async () => {
            if (botao.dataset.editarEvento && editarEvento) {
                await editarEvento(botao.dataset.editarEvento);
                return;
            }
            if (botao.dataset.exportar) {
                await baixarParticipantes(botao.dataset.eventoId, botao.dataset.exportar);
                mostrarMensagem("Participantes exportados com sucesso.");
                return;
            }

            const inscricaoId = botao.dataset.cancelarInscricao;
            const eventoId = botao.dataset.excluirEvento;
            if (!inscricaoId && !eventoId) return;
            const confirmacao = inscricaoId ? "Cancelar esta inscrição?" : "Confirma esta exclusão?";
            if (!confirm(confirmacao)) return;
            await chamarApi(inscricaoId ? `/inscricoes/${inscricaoId}` : `/eventos/${eventoId}`, "DELETE");
            if (eventoId && aoExcluirEvento) aoExcluirEvento(eventoId);
            await atualizarTela();
            mostrarMensagem("Operação concluída.");
        });
    });
}
