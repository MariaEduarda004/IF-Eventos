async function verificarResposta(resposta) {
    if (resposta.ok) {
        return;
    }

    const tipo = resposta.headers.get("content-type") || "";
    if (tipo.includes("application/json")) {
        const erro = await resposta.json();
        throw new Error(erro.erro || `Erro HTTP ${resposta.status}`);
    }
    throw new Error(`Erro HTTP ${resposta.status}: não foi possível concluir a operação.`);
}

export async function chamarApi(caminho, metodo = "GET", dados = null) {
    const configuracao = { method: metodo };
    if (dados !== null) {
        configuracao.headers = { "Content-Type": "application/json" };
        configuracao.body = JSON.stringify(dados);
    }

    const resposta = await fetch(caminho, configuracao);
    await verificarResposta(resposta);
    return resposta.status === 204 ? null : resposta.json();
}

export async function baixarParticipantes(eventoId, formato) {
    const resposta = await fetch(`/eventos/${eventoId}/exportar/${formato}`);
    await verificarResposta(resposta);
    const arquivo = await resposta.blob();
    const url = URL.createObjectURL(arquivo);
    const link = document.createElement("a");
    link.href = url;
    link.download = `participantes.${formato}`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
}
