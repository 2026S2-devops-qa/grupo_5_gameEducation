// Aba "Colaboradores e Cursos" (RF01, Bruno).
// Catalogo de cursos com filtro por area, perfil do colaborador logado e consulta por id.
// So o ADMIN ve os formularios de cadastro e a lista de colaboradores (o gateway e o Core
// tambem barram: a tela so esconde o que o usuario nao pode usar).
function colaboradorVazio() {
    return { nome: '', email: '', cargo: '', area: 'QA', anosExperiencia: 0, cursosConcluidos: 0, mediaNotas: 7.0 };
}

window.ABAS.colaboradores = {
    titulo: 'Colaboradores e Cursos',
    ordem: 10,
    componente: {
        props: ['api', 'usuario'],
        data() {
            return {
                areas: ['DEVOPS', 'QA', 'DESENVOLVIMENTO', 'DADOS', 'GESTAO'],
                niveis: ['INICIANTE', 'INTERMEDIARIO', 'AVANCADO', 'ESPECIALISTA'],
                filtroArea: '',
                cursos: [],
                perfil: null,
                colaboradores: [],
                consultaId: '',
                consultado: null,
                erroConsulta: '',
                novoCurso: { titulo: '', area: 'DEVOPS', nivel: 'INICIANTE', cargaHoraria: 20 },
                novoColaborador: colaboradorVazio(),
                mensagem: '',
                erro: false
            };
        },
        template: `
            <div>
                <div v-if="mensagem" class="msg" :class="erro ? 'err' : 'ok'">{{ mensagem }}</div>

                <!-- Catalogo -->
                <div class="card">
                    <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:12px">
                        <h2 style="margin:0">Catálogo de cursos</h2>
                        <select v-model="filtroArea" @change="carregarCursos" style="width:auto;margin:0">
                            <option value="">Todas as áreas</option>
                            <option v-for="a in areas" :key="a" :value="a">{{ a }}</option>
                        </select>
                    </div>
                    <p v-if="cursos.length === 0" class="vazio">Nenhum curso nesta área.</p>
                    <table v-else>
                        <thead><tr><th>Id</th><th>Título</th><th>Área</th><th>Nível</th><th>Carga</th></tr></thead>
                        <tbody>
                            <tr v-for="c in cursos" :key="c.id">
                                <td>{{ c.id }}</td><td>{{ c.titulo }}</td>
                                <td><span class="tag">{{ c.area }}</span></td>
                                <td>{{ c.nivel }}</td><td>{{ c.cargaHoraria }} h</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="grid" style="margin-top:20px">
                    <!-- Meu perfil (colaborador logado) -->
                    <div class="card" v-if="usuario.colaboradorId">
                        <h2>Meu perfil</h2>
                        <p v-if="!perfil" class="vazio">Carregando...</p>
                        <table v-else>
                            <tbody>
                                <tr><th>Nome</th><td>{{ perfil.nome }}</td></tr>
                                <tr><th>Email</th><td>{{ perfil.email }}</td></tr>
                                <tr><th>Cargo</th><td>{{ perfil.cargo }}</td></tr>
                                <tr><th>Área</th><td>{{ perfil.area }}</td></tr>
                                <tr><th>Experiência</th><td>{{ perfil.anosExperiencia }} anos</td></tr>
                                <tr><th>Cursos concluídos</th><td>{{ perfil.cursosConcluidos }}</td></tr>
                                <tr><th>Média das notas</th><td>{{ perfil.mediaNotas }}</td></tr>
                            </tbody>
                        </table>
                        <p class="vazio" style="margin-bottom:0">O email aparece mascarado para quem não é ADMIN (RNF03).</p>
                    </div>

                    <!-- Consulta por id: mostra o 403 quando o colaborador tenta ver outra pessoa -->
                    <div class="card">
                        <h2>Consultar colaborador</h2>
                        <label>Id do colaborador</label>
                        <input v-model.number="consultaId" type="number" min="1" placeholder="Ex.: 2" @keyup.enter="consultar" />
                        <button @click="consultar">Consultar</button>
                        <div v-if="erroConsulta" class="msg err">{{ erroConsulta }}</div>
                        <table v-if="consultado" style="margin-top:14px">
                            <tbody>
                                <tr><th>Nome</th><td>{{ consultado.nome }}</td></tr>
                                <tr><th>Email</th><td>{{ consultado.email }}</td></tr>
                                <tr><th>Cargo</th><td>{{ consultado.cargo }}</td></tr>
                                <tr><th>Área</th><td>{{ consultado.area }}</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Somente ADMIN -->
                <template v-if="usuario.admin">
                    <div class="grid" style="margin-top:20px">
                        <div class="card">
                            <h2>Cadastrar curso</h2>
                            <label>Título</label>
                            <input v-model="novoCurso.titulo" placeholder="Ex.: Terraform Essencial" />
                            <label>Área</label>
                            <select v-model="novoCurso.area"><option v-for="a in areas" :key="a" :value="a">{{ a }}</option></select>
                            <label>Nível</label>
                            <select v-model="novoCurso.nivel"><option v-for="n in niveis" :key="n" :value="n">{{ n }}</option></select>
                            <label>Carga horária (h)</label>
                            <input v-model.number="novoCurso.cargaHoraria" type="number" min="1" max="500" />
                            <button @click="cadastrarCurso">Cadastrar curso</button>
                        </div>
                        <div class="card">
                            <h2>Cadastrar colaborador</h2>
                            <label>Nome</label>
                            <input v-model="novoColaborador.nome" />
                            <label>Email</label>
                            <input v-model="novoColaborador.email" type="email" placeholder="nome@empresa.com" />
                            <label>Cargo</label>
                            <input v-model="novoColaborador.cargo" />
                            <label>Área</label>
                            <select v-model="novoColaborador.area"><option v-for="a in areas" :key="a" :value="a">{{ a }}</option></select>
                            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px">
                                <div><label>Anos exp.</label><input v-model.number="novoColaborador.anosExperiencia" type="number" min="0" /></div>
                                <div><label>Cursos</label><input v-model.number="novoColaborador.cursosConcluidos" type="number" min="0" /></div>
                                <div><label>Média</label><input v-model.number="novoColaborador.mediaNotas" type="number" min="0" max="10" step="0.1" /></div>
                            </div>
                            <button @click="cadastrarColaborador">Cadastrar colaborador</button>
                        </div>
                    </div>

                    <div class="card" style="margin-top:20px">
                        <h2>Colaboradores</h2>
                        <table>
                            <thead><tr><th>Id</th><th>Nome</th><th>Email</th><th>Cargo</th><th>Área</th><th>Exp.</th><th>Cursos</th><th>Média</th></tr></thead>
                            <tbody>
                                <tr v-for="c in colaboradores" :key="c.id">
                                    <td>{{ c.id }}</td><td>{{ c.nome }}</td><td>{{ c.email }}</td><td>{{ c.cargo }}</td>
                                    <td><span class="tag">{{ c.area }}</span></td>
                                    <td>{{ c.anosExperiencia }}</td><td>{{ c.cursosConcluidos }}</td><td>{{ c.mediaNotas }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </template>
            </div>`,
        methods: {
            notificar(texto, ehErro = false) {
                this.mensagem = texto;
                this.erro = ehErro;
            },
            async carregarCursos() {
                try {
                    const filtro = this.filtroArea ? '?area=' + encodeURIComponent(this.filtroArea) : '';
                    this.cursos = await this.api('/api/cursos' + filtro);
                } catch (e) {
                    this.notificar('Não consegui carregar os cursos: ' + e.message, true);
                }
            },
            async carregarPerfil() {
                if (!this.usuario.colaboradorId) return;
                try {
                    this.perfil = await this.api('/api/colaboradores/' + this.usuario.colaboradorId);
                } catch (e) {
                    this.notificar('Não consegui carregar o seu perfil: ' + e.message, true);
                }
            },
            async carregarColaboradores() {
                if (!this.usuario.admin) return;
                try {
                    this.colaboradores = await this.api('/api/colaboradores');
                } catch (e) {
                    this.notificar('Não consegui carregar os colaboradores: ' + e.message, true);
                }
            },
            async consultar() {
                this.consultado = null;
                this.erroConsulta = '';
                if (!this.consultaId) { this.erroConsulta = 'Informe o id.'; return; }
                try {
                    this.consultado = await this.api('/api/colaboradores/' + this.consultaId);
                } catch (e) {
                    this.erroConsulta = '(' + e.status + ') ' + e.message;
                }
            },
            async cadastrarCurso() {
                try {
                    const criado = await this.api('/api/cursos', { method: 'POST', body: this.novoCurso });
                    this.notificar('Curso "' + criado.titulo + '" cadastrado (id ' + criado.id + ').');
                    this.novoCurso = { titulo: '', area: 'DEVOPS', nivel: 'INICIANTE', cargaHoraria: 20 };
                    await this.carregarCursos();
                } catch (e) {
                    this.notificar('Falha ao cadastrar curso: ' + e.message, true);
                }
            },
            async cadastrarColaborador() {
                try {
                    const criado = await this.api('/api/colaboradores', { method: 'POST', body: this.novoColaborador });
                    this.notificar('Colaborador "' + criado.nome + '" cadastrado (id ' + criado.id + ').');
                    this.novoColaborador = colaboradorVazio();
                    await this.carregarColaboradores();
                } catch (e) {
                    this.notificar('Falha ao cadastrar colaborador: ' + e.message, true);
                }
            }
        },
        mounted() {
            this.carregarCursos();
            this.carregarPerfil();
            this.carregarColaboradores();
        }
    }
};
