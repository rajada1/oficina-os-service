package br.com.grupo99.osservice.infrastructure.persistence;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaOrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

    List<OrdemServico> findByStatus(StatusOS status);

    List<OrdemServico> findByClienteId(UUID clienteId);

    List<OrdemServico> findByVeiculoId(UUID veiculoId);

    Page<OrdemServico> findAll(Pageable pageable);
}