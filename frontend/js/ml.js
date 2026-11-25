// Aba "Competencias e ML" (RF02 e RF05, Gabriel).
// Em construcao: o Gabriel substitui este componente. Contrato das props no comentario do index.html.
// Rotas (via gateway): POST /api/colaboradores/{id}/competencia,
// POST /api/treinamentos (so ADMIN), GET /api/treinamentos, GET /api/treinamentos/{id}.
window.ABAS.ml = {
    titulo: 'Competências e ML',
    ordem: 30,
    componente: {
        props: ['api', 'usuario'],
        template: `
            <div class="card construcao">
                <h2>Competências e ML</h2>
                <p>Em construção (RF02 e RF05): classificação de competência e treinamento do modelo.</p>
            </div>`
    }
};
