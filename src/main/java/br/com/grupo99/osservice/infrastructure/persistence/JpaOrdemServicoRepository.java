package br.com.grupo99.osservice.infrastructure.persistence;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaOrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

    List<OrdemServico> findByStatus(StatusOS status);

    Page<OrdemServico> findByStatus(StatusOS status, Pageable pageable);

    List<OrdemServico> findByClienteId(UUID clienteId);

    Page<OrdemServico> findByClienteId(UUID clienteId, Pageable pageable);

    List<OrdemServico> findByVeiculoId(UUID veiculoId);

    @NonNull
    Page<OrdemServico> findAll(@NonNull Pageable pageable);
}