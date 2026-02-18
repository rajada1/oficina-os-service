package br.com.grupo99.osservice.infrastructure.controller;

import br.com.grupo99.osservice.application.dto.AtualizarStatusRequestDTO;
import br.com.grupo99.osservice.application.dto.OrdemServicoRequestDTO;
import br.com.grupo99.osservice.application.dto.OrdemServicoResponseDTO;
import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import br.com.grupo99.osservice.domain.repository.OrdemServicoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller para gerenciar Ordens de Serviço.
 * Fornece endpoints HTTP para operações CRUD e transições de status.
 */
@RestController
@RequestMapping("/api/v1/ordens-servico")
@Tag(name = "Ordem de Serviço", description = "Endpoints para gerenciar ordens de serviço")
public class OrdemServicoController {

    private final OrdemServicoRepository repository;

    public OrdemServicoController(OrdemServicoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Operation(summary = "Criar nova Ordem de Serviço")
    public ResponseEntity<OrdemServicoResponseDTO> criar(@Valid @RequestBody OrdemServicoRequestDTO request) {
        OrdemServico os = new OrdemServico(request.getClienteId(), request.getVeiculoId(),
                request.getDescricaoProblema());
        OrdemServico saved = repository.save(os);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar OS por ID")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable UUID id) {
        return repository.findById(id)
                .map(os -> ResponseEntity.ok(toResponse(os)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todas as Ordens de Serviço")
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarTodas() {
        List<OrdemServicoResponseDTO> response = repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar OS por status")
    public ResponseEntity<List<OrdemServicoResponseDTO>> buscarPorStatus(@PathVariable StatusOS status) {
        List<OrdemServicoResponseDTO> response = repository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status da OS")
    public ResponseEntity<OrdemServicoResponseDTO> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusRequestDTO request) {
        return repository.findById(id)
                .map(os -> {
                    os.atualizarStatus(request.getNovoStatus(), request.getObservacao(), request.getUsuarioAlteracao());
                    OrdemServico saved = repository.save(os);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar OS")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converte a entidade de domínio para DTO de resposta.
     */
    private OrdemServicoResponseDTO toResponse(OrdemServico os) {
        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO();
        dto.setId(os.getId());
        dto.setClienteId(os.getClienteId());
        dto.setVeiculoId(os.getVeiculoId());
        dto.setStatus(os.getStatus());
        dto.setDescricaoProblema(os.getDescricaoProblema());
        dto.setValorTotal(os.getValorTotal());
        dto.setDataCriacao(os.getDataCriacao());
        dto.setDataFinalizacao(os.getDataFinalizacao());
        dto.setDataEntrega(os.getDataEntrega());

        if (os.getHistorico() != null) {
            List<OrdemServicoResponseDTO.HistoricoStatusDTO> historico = os.getHistorico().stream()
                    .map(h -> {
                        OrdemServicoResponseDTO.HistoricoStatusDTO hDto = new OrdemServicoResponseDTO.HistoricoStatusDTO();
                        hDto.setStatusAnterior(h.getStatusAnterior());
                        hDto.setNovoStatus(h.getNovoStatus());
                        hDto.setObservacao(h.getObservacao());
                        hDto.setUsuarioAlteracao(h.getUsuarioAlteracao());
                        hDto.setDataAlteracao(h.getDataAlteracao());
                        return hDto;
                    })
                    .collect(Collectors.toList());
            dto.setHistorico(historico);
        }

        return dto;
    }
}
