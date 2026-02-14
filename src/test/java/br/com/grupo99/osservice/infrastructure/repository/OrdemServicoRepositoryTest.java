package br.com.grupo99.osservice.infrastructure.repository;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import br.com.grupo99.osservice.domain.repository.OrdemServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OrdemServicoRepository - Testes de Integração")
class OrdemServicoRepositoryTest {

    @MockBean
    private software.amazon.awssdk.services.sqs.SqsClient sqsClient;

    @Autowired
    private OrdemServicoRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Deve salvar ordem de serviço com sucesso")
    void deveSalvarOrdemServicoComSucesso() {
        // Arrange
        OrdemServico os = OrdemServico.criar(
                "João Silva",
                "joao@email.com",
                "(11) 99999-9999",
                "ABC-1234",
                "Problema no motor");

        // Act
        OrdemServico saved = repository.save(os);

        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(StatusOS.ABERTA, saved.getStatus());
    }

    @Test
    @DisplayName("Deve buscar ordem de serviço por ID")
    void deveBuscarOrdemServicoPorId() {
        // Arrange
        OrdemServico os = OrdemServico.criar(
                "Maria Santos",
                "maria@email.com",
                "(11) 88888-8888",
                "XYZ-5678",
                "Revisão periódica");
        OrdemServico saved = repository.save(os);

        // Act
        Optional<OrdemServico> found = repository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Deve buscar ordens de serviço por status")
    void deveBuscarOrdensServicoPorStatus() {
        // Arrange
        OrdemServico os1 = OrdemServico.criar("Cliente 1", "c1@email.com", "11111111111", "AAA-1111", "Desc 1");
        OrdemServico os2 = OrdemServico.criar("Cliente 2", "c2@email.com", "22222222222", "BBB-2222", "Desc 2");
        OrdemServico os3 = OrdemServico.criar("Cliente 3", "c3@email.com", "33333333333", "CCC-3333", "Desc 3");

        os2.aguardarOrcamento();
        os3.aguardarOrcamento();

        repository.saveAll(List.of(os1, os2, os3));

        // Act
        List<OrdemServico> abertas = repository.findByStatus(StatusOS.ABERTA);
        List<OrdemServico> aguardando = repository.findByStatus(StatusOS.AGUARDANDO_ORCAMENTO);

        // Assert
        assertEquals(1, abertas.size());
        assertEquals(2, aguardando.size());
    }

    @Test
    @DisplayName("Deve verificar se existe ordem de serviço")
    void deveVerificarSeExisteOrdemServico() {
        // Arrange
        OrdemServico os = OrdemServico.criar(
                "Pedro Oliveira",
                "pedro@email.com",
                "(11) 77777-7777",
                "DDD-4444",
                "Troca de óleo");
        OrdemServico saved = repository.save(os);

        // Act
        Optional<OrdemServico> found = repository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Deve atualizar status e manter histórico")
    void deveAtualizarStatusEManterHistorico() {
        // Arrange
        OrdemServico os = OrdemServico.criar(
                "Ana Costa",
                "ana@email.com",
                "(11) 66666-6666",
                "EEE-5555",
                "Alinhamento e balanceamento");
        OrdemServico saved = repository.save(os);

        // Act
        saved.aguardarOrcamento();
        OrdemServico updated = repository.save(saved);

        // Assert
        assertEquals(StatusOS.AGUARDANDO_ORCAMENTO, updated.getStatus());
        assertEquals(2, updated.getHistorico().size());
    }

    @Test
    @DisplayName("Deve deletar ordem de serviço")
    void deveDeletarOrdemServico() {
        // Arrange
        OrdemServico os = OrdemServico.criar(
                "Carlos Mendes",
                "carlos@email.com",
                "(11) 55555-5555",
                "FFF-6666",
                "Reparo elétrico");
        OrdemServico saved = repository.save(os);
        UUID id = saved.getId();

        // Act
        repository.deleteById(id);

        // Assert
        assertFalse(repository.existsById(id));
    }

    @Test
    @DisplayName("Deve listar todas as ordens de serviço")
    void deveListarTodasAsOrdensServico() {
        // Arrange
        OrdemServico os1 = OrdemServico.criar("Cliente 1", "c1@email.com", "11111111111", "GGG-7777", "Desc 1");
        OrdemServico os2 = OrdemServico.criar("Cliente 2", "c2@email.com", "22222222222", "HHH-8888", "Desc 2");
        OrdemServico os3 = OrdemServico.criar("Cliente 3", "c3@email.com", "33333333333", "III-9999", "Desc 3");

        repository.saveAll(List.of(os1, os2, os3));

        // Act
        List<OrdemServico> todas = repository.findAll();

        // Assert
        assertEquals(3, todas.size());
    }

    @Test
    @DisplayName("Deve manter integridade referencial com histórico")
    void deveManterIntegridadeReferencialComHistorico() {
        // Arrange
        OrdemServico os = OrdemServico.criar(
                "Teste Histórico",
                "teste@email.com",
                "(11) 44444-4444",
                "JJJ-1010",
                "Teste de histórico");
        os.aguardarOrcamento();
        os.aprovarOrcamento();
        os.iniciarExecucao();

        OrdemServico saved = repository.save(os);
        UUID id = saved.getId();

        // Act
        Optional<OrdemServico> found = repository.findById(id);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(4, found.get().getHistorico().size());
        assertEquals(StatusOS.EM_EXECUCAO, found.get().getStatus());
    }
}
