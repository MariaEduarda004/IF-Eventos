export function mostrarMensagem(texto, erro = false) {
    const mensagem = document.querySelector("#mensagem");
    mensagem.textContent = texto;
    mensagem.className = erro ? "mensagem visivel erro" : "mensagem visivel";
}

export function escaparHtml(valor) {
    const elemento = document.createElement("span");
    elemento.textContent = String(valor ?? "");
    return elemento.innerHTML;
}

export function preencherSelecao(selecao, itens, descricao) {
    const valorAnterior = selecao.value;
    selecao.replaceChildren(new Option("Selecione", ""));
    for (const item of itens) {
        selecao.add(new Option(descricao(item), item.id));
    }
    selecao.value = itens.some(item => String(item.id) === valorAnterior) ? valorAnterior : "";
}

export async function executar(operacao) {
    try {
        await operacao();
    } catch (erro) {
        mostrarMensagem(erro.message, true);
    }
}
