// Aba "Ranking" (feedback da AC2, Gabriel).
// Em construcao: o Gabriel substitui este componente. Contrato das props no comentario do index.html.
// Rotas (via gateway): POST /api/ranking/participacoes, GET /api/ranking?mes=AAAA-MM&top=10,
// POST /api/ranking/fechamentos/{mes} (so ADMIN).
window.ABAS.ranking = {
    titulo: 'Ranking',
    ordem: 40,
    componente: {
        props: ['api', 'usuario'],
        template: `
            <div class="card construcao">
                <h2>Ranking</h2>
                <p>Em construção: ranking mensal do fórum e bônus para o vencedor.</p>
            </div>`
    }
};
