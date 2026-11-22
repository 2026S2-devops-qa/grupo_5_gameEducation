// Aba "Gamificacao (US1)": a tela da AC1/AC2, agora passando pelo gateway com JWT.
// US1: concluir um curso com media acima de 7,0 libera 3 cursos; repetir o mesmo curso nao bonifica.
window.ABAS.alunos = {
    titulo: 'Gamificação (US1)',
    ordem: 50,
    componente: {
        props: ['api', 'usuario'],
        data() {
            return {
                mensagem: '',
                erro: false,
                alunos: [],
                novoAluno: { nome: '', plano: 'BASICO' },
                conclusao: { alunoId: '', tituloCurso: '', media: null }
            };
        },
        template: `
            <div>
                <p class="vazio" style="margin-top:0">
                    <strong>US1:</strong> concluiu um curso com média acima de 7,0, o aluno ganha
                    <strong>3 cursos liberados</strong>. Concluir o mesmo curso de novo não bonifica outra vez.
                </p>
                <div class="grid">
                    <div class="card">
                        <h2>Criar aluno</h2>
                        <label>Nome</label>
                        <input v-model="novoAluno.nome" placeholder="Ex.: Bruno da Silveira Escanhoela" @keyup.enter="criarAluno" />
                        <label>Plano</label>
                        <select v-model="novoAluno.plano">
                            <option value="BASICO">BASICO</option>
                            <option value="PREMIUM">PREMIUM</option>
                        </select>
                        <button @click="criarAluno">Criar aluno</button>
                    </div>
                    <div class="card">
                        <h2>Concluir curso</h2>
                        <label>Aluno</label>
                        <select v-model="conclusao.alunoId">
                            <option disabled value="">Selecione um aluno</option>
                            <option v-for="a in alunos" :key="a.id" :value="a.id">{{ a.id }} - {{ a.nome }}</option>
                        </select>
                        <label>Título do curso</label>
                        <input v-model="conclusao.tituloCurso" placeholder="Ex.: Fundamentos de Agile Testing" />
                        <label>Média (0 a 10)</label>
                        <input v-model.number="conclusao.media" type="number" step="0.1" min="0" max="10" placeholder="Ex.: 8.5" />
                        <button @click="concluirCurso">Registrar conclusão</button>
                    </div>
                </div>

                <div v-if="mensagem" class="msg" :class="erro ? 'err' : 'ok'">{{ mensagem }}</div>

                <div class="card" style="margin-top:20px">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
                        <h2 style="margin:0">Alunos</h2>
                        <button class="ghost" style="width:auto;padding:8px 14px" @click="listarAlunos">Atualizar</button>
                    </div>
                    <p v-if="alunos.length === 0" class="vazio">Nenhum aluno cadastrado ainda. Crie o primeiro acima.</p>
                    <table v-else>
                        <thead><tr><th>Id</th><th>Nome</th><th>Plano</th><th>Liberados</th><th>Conquistados</th></tr></thead>
                        <tbody>
                            <tr v-for="a in alunos" :key="a.id">
                                <td>{{ a.id }}</td><td>{{ a.nome }}</td>
                                <td><span class="tag">{{ a.plano }}</span></td>
                                <td><strong>{{ a.cursosLiberados }}</strong></td>
                                <td>{{ a.cursosConquistados }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>`,
        methods: {
            notificar(texto, ehErro = false) {
                this.mensagem = texto;
                this.erro = ehErro;
            },
            async listarAlunos() {
                try {
                    this.alunos = await this.api('/api/alunos');
                } catch (e) {
                    this.notificar('Não consegui carregar os alunos: ' + e.message, true);
                }
            },
            async criarAluno() {
                if (!this.novoAluno.nome.trim()) {
                    this.notificar('Informe o nome do aluno.', true);
                    return;
                }
                try {
                    const criado = await this.api('/api/alunos', { method: 'POST', body: this.novoAluno });
                    this.notificar('Aluno "' + criado.nome + '" criado (id ' + criado.id + ').');
                    this.novoAluno = { nome: '', plano: 'BASICO' };
                    await this.listarAlunos();
                    this.conclusao.alunoId = criado.id;
                } catch (e) {
                    this.notificar('Falha ao criar aluno: ' + e.message, true);
                }
            },
            async concluirCurso() {
                if (!this.conclusao.alunoId) { this.notificar('Selecione um aluno.', true); return; }
                if (!this.conclusao.tituloCurso.trim()) { this.notificar('Informe o título do curso.', true); return; }
                if (this.conclusao.media === null || isNaN(this.conclusao.media)) { this.notificar('Informe a média.', true); return; }
                try {
                    const atualizado = await this.api('/api/alunos/' + this.conclusao.alunoId + '/conclusoes', {
                        method: 'POST',
                        body: { tituloCurso: this.conclusao.tituloCurso, media: this.conclusao.media }
                    });
                    this.notificar('Conclusão registrada para "' + atualizado.nome + '": ' +
                        atualizado.cursosLiberados + ' liberado(s), ' +
                        atualizado.cursosConquistados + ' conquistado(s).');
                    this.conclusao.tituloCurso = '';
                    this.conclusao.media = null;
                    await this.listarAlunos();
                } catch (e) {
                    if (e.status === 404) {
                        this.notificar('Esse aluno não existe mais (404). Atualize a lista.', true);
                        await this.listarAlunos();
                    } else {
                        this.notificar('Falha ao concluir curso: ' + e.message, true);
                    }
                }
            }
        },
        mounted() {
            this.listarAlunos();
        }
    }
};
