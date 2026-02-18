package br.com.grupo99.osservice.infrastructure.persistence;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoRepositoryAdapter - Testes Unitários")
class OrdemServicoRepositoryAdapterTest {

    @Mock
    private JpaOrdemServicoRepository jpaRepository;

    @InjectMocks
    private OrdemServicoRepositoryAdapter adapter;

    private OrdemServico osMock;
    private UUID osId;

    @BeforeEach
    void setUp() {
        osId = UUID.randomUUID();
        osMock = new OrdemServico();
        osMock.setId(osId);
        osMock.setClienteId(UUID.randomUUID());
        osMock.setVeiculoId(UUID.randomUUID());
        osMock.setStatus(StatusOS.RECEBIDA);
        osMock.setDescricaoProblema("Problema teste");
        osMock.setValorTotal(BigDecimal.ZERO);
        osMock.setDataCriacao(LocalDateTime.now());
        osMock.setHistorico(new ArrayList<>());
    }

    @Test
    @DisplayName("Deve salvar OS delegando ao JPA repository")
    void deveSalvarOS() {
        when(jpaRepository.save(osMock)).thenReturn(osMock);

        OrdemServico result = adapter.save(osMock);

        assertThat(result).isEqualTo(osMock);
        verify(jpaRepository).save(osMock);
    }

    @Test
    @DisplayName("Deve buscar OS por ID delegando ao JPA repository")
    void deveBuscarPorId() {
        when(jpaRepository.findById(osId)).thenReturn(Optional.of(osMock));

        Optional<OrdemServico> result = adapter.findById(osId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(osId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando OS não encontrada")
    void deveRetornarVazioQuandoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<OrdemServico> result = adapter.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar page de OS")
    void deveRetornarPageDeOS() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdemServico> page = new PageImpl<>(List.of(osMock));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Page<OrdemServico> result = adapter.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar por status com paginação")
    void deveBuscarPorStatusComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdemServico> page = new PageImpl<>(List.of(osMock));
        when(jpaRepository.findByStatus(StatusOS.RECEBIDA, pageable)).thenReturn(page);

        Page<OrdemServico> result = adapter.findByStatus(StatusOS.RECEBIDA, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar por clienteId com paginação")
    void deveBuscarPorClienteIdComPaginacao() {
        UUID clienteId = osMock.getClienteId();
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdemServico> page = new PageImpl<>(List.of(osMock));
        when(jpaRepository.findByClienteId(clienteId, pageable)).thenReturn(page);

        Page<OrdemServico> result = adapter.findByClienteId(clienteId, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar por status com lista de statuses")
    void deveBuscarPorStatusIn() {
        when(jpaRepository.findAll()).thenReturn(List.of(osMock));

        List<OrdemServico> result = adapter.findByStatusIn(List.of(StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve filtrar corretamente por status no findByStatusIn")
    void deveFiltrarCorretamentePorStatusIn() {
        OrdemServico osDiagnostico = new OrdemServico();
        osDiagnostico.setId(UUID.randomUUID());
        osDiagnostico.setStatus(StatusOS.EM_DIAGNOSTICO);

        when(jpaRepository.findAll()).thenReturn(List.of(osMock, osDiagnostico));

        List<OrdemServico> result = adapter.findByStatusIn(List.of(StatusOS.EM_DIAGNOSTICO));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
    }

    @Test
    @DisplayName("Deve verificar existência por ID")
    void deveVerificarExistenciaPorId() {
        when(jpaRepository.existsById(osId)).thenReturn(true);

        boolean exists = adapter.existsById(osId);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve deletar por ID")
    void deveDeletarPorId() {
        doNothing().when(jpaRepository).deleteById(osId);

        adapter.deleteById(osId);

        verify(jpaRepository).deleteById(osId);
    }

    @Test
    @DisplayName("Deve contar por status")
    void deveContarPorStatus() {
        when(jpaRepository.findByStatus(StatusOS.RECEBIDA)).thenReturn(List.of(osMock));

        long count = adapter.countByStatus(StatusOS.RECEBIDA);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve deletar todos")
    void deveDeletarTodos() {
        doNothing().when(jpaRepository).deleteAll();

        adapter.deleteAll();

        verify(jpaRepository).deleteAll();
    }

    @Test
    @DisplayName("Deve salvar lista de OS")
    void deveSalvarListaDeOS() {
        List<OrdemServico> lista = List.of(osMock);
        when(jpaRepository.saveAll(lista)).thenReturn(lista);

        List<OrdemServico> result = adapter.saveAll(lista);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar por status sem paginação")
    void deveBuscarPorStatusSemPaginacao() {
        when(jpaRepository.findByStatus(StatusOS.RECEBIDA)).thenReturn(List.of(osMock));

        List<OrdemServico> result = adapter.findByStatus(StatusOS.RECEBIDA);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve listar todos sem paginação")
    void deveListarTodosSemPaginacao() {
        when(jpaRepository.findAll()).thenReturn(List.of(osMock));

        List<OrdemServico> result = adapter.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar false para existsByVeiculoPlaca (implementação simplificada)")
    void deveRetornarFalseParaExistsByVeiculoPlaca() {
        boolean exists = adapter.existsByVeiculoPlaca("ABC-1234");

        assertThat(exists).isFalse();
    }
}
