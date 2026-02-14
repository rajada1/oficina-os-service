package br.com.grupo99.osservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raiz: Ordem de Serviço
 * 
 * Responsável por gerenciar o ciclo de vida completo de uma OS,
 * incluindo validações de transição de status e histórico.
 */
@Entity
@Table(name = "ordem_servico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "veiculo_id", nullable = false)
    private UUID veiculoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOS status;

    @Column(name = "descricao_problema", columnDefinition = "TEXT")
    private String descricaoProblema;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @Version
    @Column(nullable = false)
    private Integer version;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HistoricoStatus> historico = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Construtor de Negócio ---

    /**
     * Construtor para criar uma nova Ordem de Serviço
     */
    public OrdemServico(UUID clienteId, UUID veiculoId, String descricaoProblema) {
        validarClienteId(clienteId);
        validarVeiculoId(veiculoId);

        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.descricaoProblema = descricaoProblema;
        this.status = StatusOS.RECEBIDA;
        this.valorTotal = BigDecimal.ZERO;
        this.dataCriacao = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.version = 0;
        this.historico = new ArrayList<>();

        // Registra primeiro histórico
        adicionarHistorico(null, StatusOS.RECEBIDA, "OS criada");
    }

    // --- Método Factory ---

    /**
     * Factory method para criar uma nova Ordem de Serviço (para testes compatíveis)
     */
    public static OrdemServico criar(String clienteNome, String clienteEmail, String clienteTelefone,
            String veiculoPlaca, String descricao) {
        validarCampoObrigatorio(clienteNome, "Cliente nome");
        validarCampoObrigatorio(clienteEmail, "Cliente email");
        validarCampoObrigatorio(clienteTelefone, "Cliente telefone");
        validarCampoObrigatorio(veiculoPlaca, "Veículo placa");
        validarCampoObrigatorio(descricao, "Descrição");

        // Gerar UUIDs fictícios para cliente e veículo (em produção viriam de
        // repositórios)
        UUID clienteId = UUID.nameUUIDFromBytes(clienteEmail.getBytes());
        UUID veiculoId = UUID.nameUUIDFromBytes(veiculoPlaca.getBytes());

        OrdemServico os = new OrdemServico();
        os.id = UUID.randomUUID();
        os.clienteId = clienteId;
        os.veiculoId = veiculoId;
        os.status = StatusOS.RECEBIDA;
        os.descricaoProblema = descricao;
        os.valorTotal = BigDecimal.ZERO;
        os.dataCriacao = LocalDateTime.now();
        os.createdAt = LocalDateTime.now();
        os.version = 0;
        os.historico = new ArrayList<>();

        // Adiciona campos extras para compatibilidade com testes
        os.clienteNome = clienteNome;
        os.clienteEmail = clienteEmail;
        os.clienteTelefone = clienteTelefone;
        os.veiculoPlaca = veiculoPlaca;

        return os;
    }

    // Campos extras para compatibilidade com testes
    @Transient
    private String clienteNome;
    @Transient
    private String clienteEmail;
    @Transient
    private String clienteTelefone;
    @Transient
    private String veiculoPlaca;

    public String getClienteNome() {
        return clienteNome;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public String getClienteTelefone() {
        return clienteTelefone;
    }

    public String getVeiculoPlaca() {
        return veiculoPlaca;
    }

    public LocalDateTime getDataAbertura() {
        return dataCriacao;
    }

    public LocalDateTime getDataConclusao() {
        return dataFinalizacao;
    }

    // --- Métodos de Negócio ---

    /**
     * Métodos de transição simplificados para testes
     */
    public void aguardarOrcamento() {
        // Se está RECEBIDA, primeiro vai para EM_DIAGNOSTICO, depois
        // AGUARDANDO_APROVACAO
        if (this.status == StatusOS.RECEBIDA) {
            atualizarStatus(StatusOS.EM_DIAGNOSTICO, "Diagnóstico iniciado", "Sistema");
        }
        atualizarStatus(StatusOS.AGUARDANDO_APROVACAO, "Aguardando aprovação do orçamento", "Sistema");
    }

    public void aprovarOrcamento() {
        atualizarStatus(StatusOS.AGUARDANDO_PAGAMENTO, "Orçamento aprovado pelo cliente", "Sistema");
    }

    public void iniciarExecucao() {
        atualizarStatus(StatusOS.EM_EXECUCAO, "Iniciando execução dos serviços", "Sistema");
    }

    public void concluir() {
        atualizarStatus(StatusOS.FINALIZADA, "Serviços concluídos", "Sistema");
    }

    /**
     * Atualiza o status da OS com validação de transição
     * 
     * @param novoStatus       Novo status desejado
     * @param observacao       Observação sobre a mudança de status
     * @param usuarioAlteracao Usuário que realizou a alteração
     * @throws IllegalStateException se a transição não for válida
     */
    public void atualizarStatus(StatusOS novoStatus, String observacao, String usuarioAlteracao) {
        Objects.requireNonNull(novoStatus, "Novo status não pode ser nulo");

        if (this.status == novoStatus) {
            throw new IllegalStateException(
                    String.format("OS já está no status %s", novoStatus));
        }

        if (!this.status.podeTransicionarPara(novoStatus)) {
            throw new IllegalStateException(
                    String.format("Transição inválida: %s -> %s", this.status, novoStatus));
        }

        StatusOS statusAnterior = this.status;
        this.status = novoStatus;
        this.updatedAt = LocalDateTime.now();

        // Atualiza datas específicas
        if (novoStatus == StatusOS.FINALIZADA) {
            this.dataFinalizacao = LocalDateTime.now();
        } else if (novoStatus == StatusOS.ENTREGUE) {
            this.dataEntrega = LocalDateTime.now();
        }

        // Registra no histórico
        adicionarHistorico(statusAnterior, novoStatus, observacao, usuarioAlteracao);
    }

    /**
     * Atualiza o valor total da OS
     */
    public void atualizarValorTotal(BigDecimal novoValor) {
        if (novoValor == null || novoValor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor total não pode ser negativo");
        }
        this.valorTotal = novoValor;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancela a OS
     */
    public void cancelar(String motivoCancelamento, String usuarioAlteracao) {
        if (this.status.isFinal()) {
            throw new IllegalStateException(
                    String.format("Não é possível cancelar OS com status %s", this.status));
        }

        StatusOS statusAnterior = this.status;
        this.status = StatusOS.CANCELADA;
        this.updatedAt = LocalDateTime.now();

        adicionarHistorico(statusAnterior, StatusOS.CANCELADA, motivoCancelamento, usuarioAlteracao);
    }

    /**
     * Sobrecarga sem parâmetros para testes
     */
    public void cancelar() {
        cancelar("Cancelamento solicitado", "Sistema");
    }

    /**
     * Adiciona entrada no histórico de status
     */
    private void adicionarHistorico(StatusOS statusAnterior, StatusOS novoStatus, String observacao) {
        adicionarHistorico(statusAnterior, novoStatus, observacao, null);
    }

    private void adicionarHistorico(StatusOS statusAnterior, StatusOS novoStatus,
            String observacao, String usuarioAlteracao) {
        HistoricoStatus historico = new HistoricoStatus(
                this,
                statusAnterior,
                novoStatus,
                observacao,
                usuarioAlteracao);
        this.historico.add(historico);
    }

    // --- Validações ---

    private void validarClienteId(UUID clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("ClienteId não pode ser nulo");
        }
    }

    private void validarVeiculoId(UUID veiculoId) {
        if (veiculoId == null) {
            throw new IllegalArgumentException("VeiculoId não pode ser nulo");
        }
    }

    private static void validarCampoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório");
        }
    }

    // --- Lifecycle Callbacks ---

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Equals & HashCode ---

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrdemServico that = (OrdemServico) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrdemServico{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", veiculoId=" + veiculoId +
                ", status=" + status +
                ", valorTotal=" + valorTotal +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
