// Aba "Assistente IA" (RF03 e RF04, Joao).
// Em construcao: o Joao substitui este componente. Contrato das props no comentario do index.html.
// Rotas (via gateway): POST /api/ia/recomendacoes, POST /api/ia/agentes/orientacao,
// POST /api/ia/agentes/acoes/{id}/autorizar | /negar, GET /api/ia/agentes/acoes.
window.ABAS.ia = {
    titulo: 'Assistente IA',
    ordem: 20,
    componente: {
        props: ['api', 'usuario'],
        template: `
            <div class="card construcao">
                <h2>Assistente IA</h2>
                <p>Em construção (RF03 e RF04): recomendação de cursos com RAG e agentes de orientação.</p>
            </div>`
    }
};
