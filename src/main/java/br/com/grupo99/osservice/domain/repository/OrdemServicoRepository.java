package br.com.grupo99.osservice.domain.repository;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de domínio para Ordem de Serviço.
 * Define contrato sem dependência de tecnologia de persistência.
 */
public interface OrdemServicoRepository {

    OrdemServico save(OrdemServico ordemServico);

    Optional<OrdemServico> findById(UUID id);

    Page<OrdemServico> findAll(Pageable pageable);

    Page<OrdemServico> findByStatus(StatusOS status, Pageable pageable);

    Page<OrdemServico> findByClienteId(UUID clienteId, Pageable pageable);

    List<OrdemServico> findByStatusIn(List<StatusOS> statuses);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    long countByStatus(StatusOS status);

    // Métodos de compatibilidade com testes
    void deleteAll();

    List<OrdemServico> saveAll(Iterable<OrdemServico> ordens);

    List<OrdemServico> findByStatus(StatusOS status);

    List<OrdemServico> findAll();

    boolean existsByVeiculoPlaca(String placa);
}
