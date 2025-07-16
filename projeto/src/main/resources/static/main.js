// main.js

const API_BASE_URL = 'http://localhost:8085/api/projetos'; // Ajuste a porta se necessário
const REPORT_API_URL = 'http://localhost:8085/api/relatorios/resumo';
let username = '';
let password = '';
let currentPage = 0;
const pageSize = 10; // Tamanho da página para listagem

// Elementos do DOM
const loginSection = document.getElementById('login-section');
const appContent = document.getElementById('app-content');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const createProjectForm = document.getElementById('create-project-form');
const createProjectMessage = document.getElementById('create-project-message');
const projectsTableBody = document.getElementById('projectsTableBody');
const listProjectsMessage = document.getElementById('list-projects-message');
const prevPageBtn = document.getElementById('prevPageBtn');
const nextPageBtn = document.getElementById('nextPageBtn');
const pageInfo = document.getElementById('pageInfo');
const applyFiltersBtn = document.getElementById('applyFiltersBtn');

// Modais de Status (Bootstrap Modals)
const statusModalElement = document.getElementById('statusModal');
const statusModal = new bootstrap.Modal(statusModalElement); // Inicializa o modal Bootstrap
const modalProjectId = document.getElementById('modalProjectId');
const modalProjectName = document.getElementById('modalProjectName');
const modalCurrentStatus = document.getElementById('modalCurrentStatus');
const newStatusSelect = document.getElementById('newStatus');
const saveStatusUpdateBtn = document.getElementById('saveStatusUpdateBtn');
const statusUpdateMessage = document.getElementById('status-update-message');

// Modais de Alocação de Membros (Bootstrap Modals)
const allocateMembersModalElement = document.getElementById('allocateMembersModal');
const allocateMembersModal = new bootstrap.Modal(allocateMembersModalElement);
const allocateModalProjectId = document.getElementById('allocateModalProjectId');
const allocateModalProjectName = document.getElementById('allocateModalProjectName');
const memberIdsInput = document.getElementById('memberIdsInput');
const saveAllocateMembersBtn = document.getElementById('saveAllocateMembersBtn');
const allocateMembersMessage = document.getElementById('allocate-members-message');

// Modal de Listagem de Membros Alocados (Bootstrap Modals)
const listMembersModalElement = document.getElementById('listMembersModal');
const listMembersModal = new bootstrap.Modal(listMembersModalElement);
const listMembersModalProjectName = document.getElementById('listMembersModalProjectName');
const allocatedMembersList = document.getElementById('allocatedMembersList');
const listMembersMessage = document.getElementById('list-members-message');

// NOVO: Elementos do Relatório
const generateReportBtn = document.getElementById('generateReportBtn');
const portfolioSummaryModalElement = document.getElementById('portfolioSummaryModal');
const portfolioSummaryModal = new bootstrap.Modal(portfolioSummaryModalElement);
const portfolioSummaryMessage = document.getElementById('portfolio-summary-message');
const projectsByStatusList = document.getElementById('projectsByStatusList');
const totalBudgetByStatusList = document.getElementById('totalBudgetByStatusList');
const averageDuration = document.getElementById('averageDuration');
const totalUniqueMembers = document.getElementById('totalUniqueMembers');


// --- Funções de Autenticação ---

// Função auxiliar para fazer requisições autenticadas
async function authenticatedFetch(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + btoa(username + ':' + password),
        ...options.headers
    };
    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
        showLoginScreen('Sessão expirada ou credenciais inválidas. Por favor, faça login novamente.');
        throw new Error("Não autorizado. Redirecionando para o login.");
    }

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || `Erro na requisição: ${response.status}`);
    }
    return response;
}

// Exibe a tela de login e esconde o conteúdo da aplicação
function showLoginScreen(message = '') {
    appContent.classList.add('d-none');
    loginSection.classList.remove('d-none');
    loginError.textContent = message;
    loginError.classList.remove('d-none');
    username = '';
    password = '';
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
}

// Lida com o envio do formulário de login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    username = document.getElementById('username').value;
    password = document.getElementById('password').value;
    loginError.classList.add('d-none'); // Esconde erros anteriores

    try {
        // Tenta fazer uma requisição simples para validar as credenciais
        const testResponse = await authenticatedFetch(`${API_BASE_URL}?page=0&size=1`);
        if (testResponse.ok) {
            loginSection.classList.add('d-none');
            appContent.classList.remove('d-none');
            await fetchProjects(); // Carrega os projetos após o login bem-sucedido
        } else {
            loginError.textContent = 'Usuário ou senha inválidos.';
            loginError.classList.remove('d-none');
        }
    } catch (error) {
        console.error("Erro durante o login de teste:", error);
        if (!loginError.classList.contains('d-none')) {
            loginError.textContent = 'Falha no login. Verifique suas credenciais.';
        } else {
            loginError.textContent = `Erro ao tentar login: ${error.message}`;
        }
        loginError.classList.remove('d-none');
    }
});

// --- Funções de API ---

// Criar Projeto
createProjectForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    createProjectMessage.textContent = '';
    createProjectMessage.classList.remove('text-success', 'text-danger');

    const formData = new FormData(createProjectForm);
    const projectData = Object.fromEntries(formData.entries());

    if (projectData.startDate) projectData.startDate = formatDate(projectData.startDate);
    if (projectData.forecastEndDate) projectData.forecastEndDate = formatDate(projectData.forecastEndDate);
    if (projectData.actualEndDate) projectData.actualEndDate = formatDate(projectData.actualEndDate);

    projectData.totalBudget = parseFloat(projectData.totalBudget);
    projectData.managerId = parseInt(projectData.managerId);

    try {
        const response = await authenticatedFetch(API_BASE_URL, {
            method: 'POST',
            body: JSON.stringify(projectData)
        });
        const newProject = await response.json();
        createProjectMessage.textContent = `Projeto "${newProject.name}" criado com sucesso!`;
        createProjectMessage.classList.add('text-success');
        createProjectForm.reset();
        await fetchProjects();
    } catch (error) {
        createProjectMessage.textContent = `Erro ao criar projeto: ${error.message}`;
        createProjectMessage.classList.add('text-danger');
    }
});

// Listar Projetos
async function fetchProjects() {
    listProjectsMessage.textContent = 'Carregando projetos...';
    listProjectsMessage.classList.remove('text-success', 'text-danger');
    projectsTableBody.innerHTML = '';

    const name = document.getElementById('filterName').value;
    const status = document.getElementById('filterStatus').value;
    const managerId = document.getElementById('filterManagerId').value;

    let url = `${API_BASE_URL}?page=${currentPage}&size=${pageSize}`;
    if (name) url += `&name=${encodeURIComponent(name)}`;
    if (status) url += `&status=${encodeURIComponent(status)}`;
    if (managerId) url += `&managerId=${encodeURIComponent(managerId)}`;

    try {
        const response = await authenticatedFetch(url);
        const data = await response.json();

        if (data.content && data.content.length > 0) {
            data.content.forEach(project => {
                const row = projectsTableBody.insertRow();
                row.innerHTML = `
                    <td>${project.id}</td>
                    <td>${project.name}</td>
                    <td>${project.managerName || 'N/A'} (ID: ${project.managerId})</td>
                    <td>${project.status ? project.status.description : 'N/A'}</td>
                    <td>R$ ${project.totalBudget ? project.totalBudget.toFixed(2) : '0.00'}</td>
                    <td>${project.riskLevel ? project.riskLevel.description : 'N/A'}</td>
                    <td class="table-actions">
                        <button data-id="${project.id}" data-name="${project.name}" data-status="${project.status ? project.status.name : ''}" class="btn btn-sm btn-secondary update-status-btn">Status</button>
                        <button data-id="${project.id}" data-name="${project.name}" class="btn btn-sm btn-success allocate-members-btn">Alocar</button>
                        <button data-id="${project.id}" data-name="${project.name}" class="btn btn-sm btn-info list-members-btn">Membros</button>
                        <button data-id="${project.id}" class="btn btn-sm btn-danger delete-project-btn">Excluir</button>
                    </td>
                `;
            });
            listProjectsMessage.textContent = '';
        } else {
            projectsTableBody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">Nenhum projeto encontrado.</td></tr>`;
            listProjectsMessage.textContent = '';
        }

        pageInfo.textContent = `Página ${data.number + 1} de ${data.totalPages}`;
        prevPageBtn.disabled = data.first;
        nextPageBtn.disabled = data.last;

    } catch (error) {
        listProjectsMessage.textContent = `Erro ao carregar projetos: ${error.message}`;
        listProjectsMessage.classList.add('text-danger');
    }
}

// Event Listeners para Paginação e Filtros
prevPageBtn.addEventListener('click', async () => {
    if (currentPage > 0) {
        currentPage--;
        await fetchProjects();
    }
});

nextPageBtn.addEventListener('click', async () => {
    currentPage++;
    await fetchProjects();
});

applyFiltersBtn.addEventListener('click', async () => {
    currentPage = 0;
    await fetchProjects();
});

// --- Funções para Modais ---

// Abrir Modal de Status
projectsTableBody.addEventListener('click', (e) => {
    if (e.target.classList.contains('update-status-btn')) {
        const projectId = e.target.dataset.id;
        const projectName = e.target.dataset.name;
        const currentStatus = e.target.dataset.status;

        modalProjectId.value = projectId;
        modalProjectName.textContent = projectName;
        modalCurrentStatus.textContent = currentStatus.replace(/_/g, ' ');
        newStatusSelect.value = currentStatus;
        statusUpdateMessage.textContent = '';
        statusUpdateMessage.classList.remove('text-success', 'text-danger');
        statusModal.show(); // Exibe o modal Bootstrap
    }
});

// Salvar Atualização de Status
saveStatusUpdateBtn.addEventListener('click', async () => {
    const projectId = modalProjectId.value;
    const newStatus = newStatusSelect.value;
    statusUpdateMessage.textContent = '';
    statusUpdateMessage.classList.remove('text-success', 'text-danger');

    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/${projectId}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ newStatus: newStatus })
        });
        const updatedProject = await response.json();
        statusUpdateMessage.textContent = `Status do projeto "${updatedProject.name}" atualizado para "${updatedProject.status.description}"!`;
        statusUpdateMessage.classList.add('text-success');
        await fetchProjects();
        setTimeout(() => statusModal.hide(), 1500); // Esconde após um tempo
    } catch (error) {
        statusUpdateMessage.textContent = `Erro ao atualizar status: ${error.message}`;
        statusUpdateMessage.classList.add('text-danger');
    }
});

// Excluir Projeto
projectsTableBody.addEventListener('click', async (e) => {
    if (e.target.classList.contains('delete-project-btn')) {
        const projectId = e.target.dataset.id;
        if (confirm(`Tem certeza que deseja excluir o projeto ID ${projectId}?`)) {
            try {
                await authenticatedFetch(`${API_BASE_URL}/${projectId}`, {
                    method: 'DELETE'
                });
                listProjectsMessage.textContent = `Projeto ID ${projectId} excluído com sucesso!`;
                listProjectsMessage.classList.add('text-success');
                await fetchProjects();
            } catch (error) {
                listProjectsMessage.textContent = `Erro ao excluir projeto: ${error.message}`;
                listProjectsMessage.classList.add('text-danger');
            }
        }
    }
});

// Abrir Modal de Alocação de Membros
projectsTableBody.addEventListener('click', (e) => {
    if (e.target.classList.contains('allocate-members-btn')) {
        const projectId = e.target.dataset.id;
        const projectName = e.target.dataset.name;

        allocateModalProjectId.value = projectId;
        allocateModalProjectName.textContent = projectName;
        memberIdsInput.value = '';
        allocateMembersMessage.textContent = '';
        allocateMembersMessage.classList.remove('text-success', 'text-danger');
        allocateMembersModal.show(); // Exibe o modal Bootstrap
    }
});

// Salvar Alocação de Membros
saveAllocateMembersBtn.addEventListener('click', async () => {
    const projectId = allocateModalProjectId.value;
    const memberIdsText = memberIdsInput.value;
    const memberIds = memberIdsText.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));

    allocateMembersMessage.textContent = '';
    allocateMembersMessage.classList.remove('text-success', 'text-danger');

    if (memberIds.length === 0) {
        allocateMembersMessage.textContent = 'Por favor, insira IDs de membros válidos.';
        allocateMembersMessage.classList.add('text-danger');
        return;
    }

    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/${projectId}/membros`, {
            method: 'POST',
            body: JSON.stringify(memberIds)
        });
        const updatedProject = await response.json();
        allocateMembersMessage.textContent = `Membros alocados ao projeto "${updatedProject.name}" com sucesso!`;
        allocateMembersMessage.classList.add('text-success');
        await fetchProjects();
        setTimeout(() => allocateMembersModal.hide(), 1500);
    } catch (error) {
        allocateMembersMessage.textContent = `Erro ao alocar membros: ${error.message}`;
        allocateMembersMessage.classList.add('text-danger');
    }
});

// Abrir Modal de Listagem de Membros Alocados
projectsTableBody.addEventListener('click', async (e) => {
    if (e.target.classList.contains('list-members-btn')) {
        const projectId = e.target.dataset.id;
        const projectName = e.target.dataset.name;

        listMembersModalProjectName.textContent = projectName;
        allocatedMembersList.innerHTML = '';
        listMembersMessage.textContent = 'Carregando membros...';
        listMembersMessage.classList.remove('text-success', 'text-danger');
        listMembersModal.show(); // Exibe o modal Bootstrap

        try {
            const response = await authenticatedFetch(`${API_BASE_URL}/${projectId}/membros`);
            const members = await response.json();

            if (members && members.length > 0) {
                members.forEach(member => {
                    const li = document.createElement('li');
                    li.classList.add('list-group-item'); // Classe Bootstrap para itens de lista
                    li.textContent = `${member.memberName} (ID: ${member.memberId})`;
                    allocatedMembersList.appendChild(li);
                });
                listMembersMessage.textContent = '';
            } else {
                allocatedMembersList.innerHTML = '<li class="list-group-item text-muted">Nenhum membro alocado.</li>';
                listMembersMessage.textContent = '';
            }
        } catch (error) {
            listMembersMessage.textContent = `Erro ao carregar membros: ${error.message}`;
            listMembersMessage.classList.add('text-danger');
        }
    }
});

// --- NOVO: Lógica para o Relatório ---
generateReportBtn.addEventListener('click', async () => {
    portfolioSummaryMessage.textContent = 'Gerando relatório...';
    portfolioSummaryMessage.classList.remove('text-success', 'text-danger');
    projectsByStatusList.innerHTML = '';
    totalBudgetByStatusList.innerHTML = '';
    averageDuration.textContent = '';
    totalUniqueMembers.textContent = '';

    try {
        const response = await authenticatedFetch(REPORT_API_URL);
        const summary = await response.json();

        // 1. Quantidade de projetos por status
        if (summary.projectsByStatus && Object.keys(summary.projectsByStatus).length > 0) {
            for (const status in summary.projectsByStatus) {
                const li = document.createElement('li');
                li.classList.add('list-group-item');
                li.textContent = `${status}: ${summary.projectsByStatus[status]}`;
                projectsByStatusList.appendChild(li);
            }
        } else {
            projectsByStatusList.innerHTML = '<li class="list-group-item text-muted">Nenhum dado de projetos por status.</li>';
        }

        // 2. Total orçado por status
        if (summary.totalBudgetByStatus && Object.keys(summary.totalBudgetByStatus).length > 0) {
            for (const status in summary.totalBudgetByStatus) {
                const li = document.createElement('li');
                li.classList.add('list-group-item');
                li.textContent = `${status}: R$ ${parseFloat(summary.totalBudgetByStatus[status]).toFixed(2)}`;
                totalBudgetByStatusList.appendChild(li);
            }
        } else {
            totalBudgetByStatusList.innerHTML = '<li class="list-group-item text-muted">Nenhum dado de orçamento por status.</li>';
        }

        // 3. Média de duração dos projetos encerrados
        if (summary.averageDurationOfFinishedProjects !== null && summary.averageDurationOfFinishedProjects !== undefined) {
            averageDuration.textContent = `${summary.averageDurationOfFinishedProjects.toFixed(2)} dias`;
        } else {
            averageDuration.textContent = 'N/A';
        }

        // 4. Total de membros únicos alocados
        if (summary.totalUniqueMembersAllocated !== null && summary.totalUniqueMembersAllocated !== undefined) {
            totalUniqueMembers.textContent = summary.totalUniqueMembersAllocated;
        } else {
            totalUniqueMembers.textContent = 'N/A';
        }

        portfolioSummaryMessage.textContent = 'Relatório gerado com sucesso!';
        portfolioSummaryMessage.classList.add('text-success');
        portfolioSummaryModal.show(); // Exibe o modal de resumo

    } catch (error) {
        portfolioSummaryMessage.textContent = `Erro ao gerar relatório: ${error.message}`;
        portfolioSummaryMessage.classList.add('text-danger');
    }
});


// --- Funções Utilitárias ---
function formatDate(dateString) {
    const [year, month, day] = dateString.split('-');
    return `${day}/${month}/${year}`;
}

// Inicializa a aplicação
document.addEventListener('DOMContentLoaded', () => {
    showLoginScreen();
});
