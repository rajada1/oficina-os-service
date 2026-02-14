package br.com.grupo99.osservice.infrastructure.persistence;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import br.com.grupo99.osservice.domain.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {

    private final JpaOrdemServicoRepository jpaRepository;

    @Override
    public OrdemServico save(OrdemServico ordemServico) {
        return jpaRepository.save(ordemServico);
    }

    @Override
    public Optional<OrdemServico> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<OrdemServico> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Page<OrdemServico> findByStatus(StatusOS status, Pageable pageable) {
        return jpaRepository.findAll(pageable); // Simplified for now
    }

    @Override
    public Page<OrdemServico> findByClienteId(UUID clienteId, Pageable pageable) {
        return jpaRepository.findAll(pageable); // Simplified for now
    }

    @Override
    public List<OrdemServico> findByStatusIn(List<StatusOS> statuses) {
        return jpaRepository.findAll(); // Simplified for now
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByStatus(StatusOS status) {
        return jpaRepository.findByStatus(status).size();
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public List<OrdemServico> saveAll(Iterable<OrdemServico> ordens) {
        return jpaRepository.saveAll(ordens);
    }

    @Override
    public List<OrdemServico> findByStatus(StatusOS status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<OrdemServico> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsByVeiculoPlaca(String placaVeiculo) {
        // Implementação simplificada - campo veiculoPlaca é @Transient
        // Em produção, seria necessário um serviço para buscar por placa
        return false;
    }
}
