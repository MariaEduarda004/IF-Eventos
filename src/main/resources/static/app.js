const formularioUsuario = document.querySelector("#formUsuario");
const formularioEvento = document.querySelector("#formEvento");
const formularioInscricao = document.querySelector("#formInscricao");
const mensagem = document.querySelector("#mensagem");

// Faz uma requisição HTTP e converte a resposta JSON para objeto js
async function chamarApi(caminho, metodo = "GET", dados = null) {
    const configuracao = {
        method: metodo,
        headers: {
            "Content-Type": "application/json"
        }
    };

    if (dados !== null) {
        configuracao.body = JSON.stringify(dados);
    }

    const resposta = await fetch(caminho, configuracao);

    if (resposta.status === 204) {
        return null;
    }

    const resultado = await resposta.json();

    if (!resposta.ok) {
        throw new Error(resultado.erro || "Não foi possível concluir a operação");
    }

    return resultado;
}

function mostrarMensagem(texto, erro = false) {
    mensagem.textContent = texto;
    mensagem.className = "mensagem visivel";
    if(erro){
        mensagem.className = "mensagem visivel erro";
    }
}

// Evita inserir texto fornecido por usuários diretamente como HTML.
function escaparHtml(valor) {
    const elemento = document.createElement("span");
    elemento.textContent = String(valor ?? "");
    return elemento.innerHTML;
}

function criarOpcoes(itens, descricao) {
    const opcoes = itens.map(item => {
        return `<option value="${item.id}">${escaparHtml(descricao(item))}</option>`;
    });

    return `<option value="">Selecione</option>${opcoes.join("")}`;
}

async function atualizarTela() {
    const usuarios = await chamarApi("/usuarios");
    const eventos = await chamarApi("/eventos");
    document.querySelector("#usuarioId").innerHTML = criarOpcoes(usuarios,usuario => usuario.nome);
    document.querySelector("#eventoId").innerHTML = criarOpcoes(eventos,evento => evento.titulo);
    mostrarUsuarios(usuarios);
    await mostrarEventos(eventos);
}

function mostrarUsuarios(usuarios) {
    const lista = document.querySelector("#listaUsuarios");
    if (usuarios.length === 0) {
        lista.innerHTML = '<p class="vazio">Nenhum usuário cadastrado.</p>';
        return;
    }
    lista.innerHTML = usuarios.map(usuario => `
        <div class="item-usuario">
            <span><strong>${escaparHtml(usuario.nome)}</strong> — ${escaparHtml(usuario.email)}</span>
            <button class="botao-pequeno botao-perigo" data-excluir-usuario="${usuario.id}">Excluir</button>
        </div>
    `).join("");
}

async function mostrarEventos(eventos) {
    const lista = document.querySelector("#listaEventos");
    if (eventos.length === 0) {
        lista.innerHTML = '<p class="vazio">Nenhum evento cadastrado.</p>';
        return;
    }
    const cartoes = [];
    for (const evento of eventos) {
        const inscricoes = await chamarApi(`/inscricoes/evento/${evento.id}`);
        const percentual = (evento.inscritos / evento.capacidade) * 100;
        const vagas = evento.vagasDisponiveis;
        const participantes = inscricoes.map(inscricao => `
            <div class="item-inscricao">
                <span>${escaparHtml(inscricao.usuarioNome)}</span>
                <button class="botao-pequeno botao-perigo" data-cancelar-inscricao="${inscricao.id}"> Cancelar </button>
            </div>
        `).join("");

        cartoes.push(`
            <article class="cartao">
                <h3>${escaparHtml(evento.titulo)}</h3>
                <p>${escaparHtml(evento.descricao || "Sem descrição")}</p>
                <p><strong>Data:</strong> ${escaparHtml(evento.data)}</p>
                <p><strong>Local:</strong> ${escaparHtml(evento.local)}</p>
                <p><strong>Inscritos:</strong> ${evento.inscritos} / ${evento.capacidade}</p>
                <div class="barra">
                    <div class="progresso" style="width: ${percentual}%">
                    </div>
                </div>
                <p class="${vagas === 0 ? "aviso" : ""}"> ${vagas === 0 ? "Vagas esgotadas" : `${vagas} vaga(s) disponível(is)`} </p>
                <div class="subtitulo">Participantes</div>
                <button class="botao-pequeno" data-exportar-json="${evento.id}"> Exportar participantes em JSON </button>
                ${participantes || '<p class="vazio">Sem inscritos.</p>'}
                <p><button class="botao-pequeno botao-perigo" data-excluir-evento="${evento.id}">Excluir evento</button></p>
            </article>
        `);
    }
    lista.innerHTML = cartoes.join("");
}

// Captura os dados do formulário de usuário e envia POST /usuarios.
formularioUsuario.addEventListener("submit", async evento => {
    evento.preventDefault();
    const dados = new FormData(formularioUsuario);
    const usuario = {
        nome: dados.get("nome"),
        email: dados.get("email"),
        idade: Number(dados.get("idade"))
    };

    try {
        await chamarApi("/usuarios", "POST", usuario);
        formularioUsuario.reset();
        await atualizarTela();
        mostrarMensagem("Usuário cadastrado com sucesso.");
    } catch (erro) {
        mostrarMensagem(erro.message, true);
    }
});

formularioEvento.addEventListener("submit", async evento => {
    evento.preventDefault();
    const dados = new FormData(formularioEvento);
    const novoEvento = {
        titulo: dados.get("titulo"),
        descricao: dados.get("descricao"),
        data: dados.get("data"),
        local: dados.get("local"),
        capacidade: Number(dados.get("capacidade"))
    };
    try {
        await chamarApi("/eventos", "POST", novoEvento);
        formularioEvento.reset();
        await atualizarTela();
        mostrarMensagem("Evento cadastrado com sucesso.");
    } catch (erro) {
        mostrarMensagem(erro.message, true);
    }
});

formularioInscricao.addEventListener("submit", async evento => {
    evento.preventDefault();
    const inscricao = {
        usuarioId: Number(document.querySelector("#usuarioId").value),
        eventoId: Number(document.querySelector("#eventoId").value)
    };
    try {
        await chamarApi("/inscricoes", "POST", inscricao);
        await atualizarTela();
        mostrarMensagem("Inscrição realizada com sucesso.");
    } catch (erro) {
        mostrarMensagem(erro.message, true);
    }
});

// Um único listener cuida dos botões de cancelar e excluir.
document.addEventListener("click", async evento => {
    const botao = evento.target.closest("button");
    if (!botao) {
        return;
    }
    if (botao.dataset.exportarJson) {
        const eventoId = botao.dataset.exportarJson;
        try {
            await exportarParticipantes(eventoId);
            mostrarMensagem(
                "Participantes exportados com sucesso."
            );
        } catch (erro) {
            mostrarMensagem(erro.message, true);
        }
        return;
    }
    let caminho = null;
    let confirmacao = "Confirma esta exclusão?";
    if (botao.dataset.cancelarInscricao) {
        caminho = `/inscricoes/${botao.dataset.cancelarInscricao}`;
        confirmacao = "Cancelar esta inscrição?";
    } else if (botao.dataset.excluirEvento) {
        caminho = `/eventos/${botao.dataset.excluirEvento}`;
    } else if (botao.dataset.excluirUsuario) {
        caminho = `/usuarios/${botao.dataset.excluirUsuario}`;
    }
    if (!caminho || !confirm(confirmacao)) {
        return;
    }
    try {
        await chamarApi(caminho, "DELETE");
        await atualizarTela();
        mostrarMensagem("Operação concluída.");
    } catch (erro) {
        mostrarMensagem(erro.message, true);
    }
});
document.querySelector("#atualizar").addEventListener("click", async () => {
    try {
        await atualizarTela();
        mostrarMensagem("Dados atualizados.");
    } catch (erro) {
        mostrarMensagem(erro.message, true);
    }
});

async function exportarParticipantes(eventoId) {
    const url = `/eventos/${eventoId}/exportar/json`;
    const resposta = await fetch(url);
    if (!resposta.ok) {
        throw new Error("Não foi possível exportar os participantes.");
    }
    const arquivo = await resposta.blob();
    const urlArquivo = URL.createObjectURL(arquivo);
    const link = document.createElement("a");
    link.href = urlArquivo;
    link.download = "participantes.json";
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(urlArquivo);
}
atualizarTela().catch(erro => mostrarMensagem(erro.message, true));
