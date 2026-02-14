package br.com.grupo99.osservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa o histórico de mudanças de status de uma OS.
 * Fornece auditoria completa do ciclo de vida da ordem de serviço.
 */
@Entity
@Table(name = "historico_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusOS statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "novo_status", nullable = false, length = 30)
    private StatusOS novoStatus;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "usuario_alteracao", length = 100)
    private String usuarioAlteracao;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    // --- Construtor de Negócio ---

    public HistoricoStatus(OrdemServico ordemServico, StatusOS statusAnterior,
            StatusOS novoStatus, String observacao, String usuarioAlteracao) {
        this.ordemServico = ordemServico;
        this.statusAnterior = statusAnterior;
        this.novoStatus = novoStatus;
        this.observacao = observacao;
        this.usuarioAlteracao = usuarioAlteracao;
        this.dataAlteracao = LocalDateTime.now();
    }

    // Métodos de compatibilidade com testes
    public StatusOS getStatusNovo() {
        return novoStatus;
    }

    public LocalDateTime getDataTransicao() {
        return dataAlteracao;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dataAlteracao == null) {
            this.dataAlteracao = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HistoricoStatus that = (HistoricoStatus) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HistoricoStatus{" +
                "id=" + id +
                ", statusAnterior=" + statusAnterior +
                ", novoStatus=" + novoStatus +
                ", dataAlteracao=" + dataAlteracao +
                '}';
    }
}
