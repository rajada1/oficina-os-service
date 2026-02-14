package br.com.grupo99.osservice.infrastructure.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementação customizada de UserDetails para armazenar informações do JWT.
 * 
 * Seguindo os princípios:
 * - Single Responsibility: Encapsula apenas dados do usuário autenticado
 * - Immutability: Todos os campos são final para garantir thread-safety
 * - Encapsulation: Dados privados com getters públicos
 */
public class JwtUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final UUID pessoaId;
    private final String numeroDocumento;
    private final String tipoPessoa;
    private final String cargo;
    private final String perfil;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Construtor padrão para uso do Spring.
     */
    public JwtUserDetails() {
        this.username = "";
        this.password = null;
        this.pessoaId = null;
        this.numeroDocumento = "";
        this.tipoPessoa = null;
        this.cargo = null;
        this.perfil = null;
        this.authorities = Collections.emptyList();
    }

    private JwtUserDetails(String username, String password, UUID pessoaId, String numeroDocumento,
            String tipoPessoa, String cargo, String perfil) {
        this.username = username;
        this.password = password;
        this.pessoaId = pessoaId;
        this.numeroDocumento = numeroDocumento;
        this.tipoPessoa = tipoPessoa;
        this.cargo = cargo;
        this.perfil = perfil;
        this.authorities = perfil != null
                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + perfil))
                : Collections.emptyList();
    }

    /**
     * Factory method para criar JwtUserDetails a partir dos claims do JWT.
     *
     * @param username        Nome de usuário (subject do JWT)
     * @param pessoaId        ID da pessoa
     * @param numeroDocumento Número do documento (CPF/CNPJ)
     * @param tipoPessoa      Tipo de pessoa (FISICA/JURIDICA)
     * @param cargo           Cargo da pessoa
     * @param perfil          Perfil de acesso (CLIENTE/MECANICO/ADMIN)
     * @return Instância de JwtUserDetails
     */
    public static JwtUserDetails from(String username, String pessoaId, String numeroDocumento,
            String tipoPessoa, String cargo, String perfil) {
        Objects.requireNonNull(username, "Username não pode ser nulo");
        Objects.requireNonNull(pessoaId, "PessoaId não pode ser nulo");
        Objects.requireNonNull(numeroDocumento, "Número de documento não pode ser nulo");
        Objects.requireNonNull(tipoPessoa, "Tipo de pessoa não pode ser nulo");
        Objects.requireNonNull(perfil, "Perfil não pode ser nulo");

        UUID pessoaUuid;
        try {
            pessoaUuid = UUID.fromString(pessoaId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("PessoaId inválido: " + pessoaId, e);
        }

        return new JwtUserDetails(username, null, pessoaUuid, numeroDocumento, tipoPessoa, cargo, perfil);
    }

    /**
     * Factory method para criar JwtUserDetails com senha (usado no login).
     *
     * @param username        Nome de usuário
     * @param password        Senha codificada
     * @param pessoaId        ID da pessoa
     * @param numeroDocumento Número do documento (CPF/CNPJ)
     * @param tipoPessoa      Tipo de pessoa (FISICA/JURIDICA)
     * @param cargo           Cargo da pessoa
     * @param perfil          Perfil de acesso (CLIENTE/MECANICO/ADMIN)
     * @return Instância de JwtUserDetails
     */
    public static JwtUserDetails withPassword(String username, String password, String pessoaId,
            String numeroDocumento, String tipoPessoa,
            String cargo, String perfil) {
        Objects.requireNonNull(username, "Username não pode ser nulo");
        Objects.requireNonNull(password, "Password não pode ser nulo");
        Objects.requireNonNull(pessoaId, "PessoaId não pode ser nulo");
        Objects.requireNonNull(numeroDocumento, "Número de documento não pode ser nulo");
        Objects.requireNonNull(tipoPessoa, "Tipo de pessoa não pode ser nulo");
        Objects.requireNonNull(perfil, "Perfil não pode ser nulo");

        UUID pessoaUuid;
        try {
            pessoaUuid = UUID.fromString(pessoaId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("PessoaId inválido: " + pessoaId, e);
        }

        return new JwtUserDetails(username, password, pessoaUuid, numeroDocumento, tipoPessoa, cargo, perfil);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Retorna o perfil do usuário.
     */
    public String getPerfil() {
        return perfil;
    }

    /**
     * Retorna o ID da pessoa.
     */
    public UUID getPessoaId() {
        return pessoaId;
    }

    /**
     * Retorna o número do documento.
     */
    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    /**
     * Retorna o tipo de pessoa.
     */
    public String getTipoPessoa() {
        return tipoPessoa;
    }

    /**
     * Retorna o cargo da pessoa.
     */
    public String getCargo() {
        return cargo;
    }

    /**
     * Verifica se o usuário tem permissão de mecânico ou superior.
     */
    public boolean isMecanicoOrHigher() {
        return perfil != null && ("MECANICO".equals(perfil) || "ADMIN".equals(perfil));
    }

    /**
     * Verifica se o usuário é um cliente.
     */
    public boolean isCliente() {
        return perfil != null && "CLIENTE".equals(perfil);
    }

    /**
     * Verifica se o usuário é dono de uma determinada pessoa.
     * 
     * @param pessoaId ID da pessoa a verificar
     * @return true se for o dono ou se tiver permissão de mecânico
     */
    public boolean isOwnerOrMecanico(UUID pessoaId) {
        if (isMecanicoOrHigher()) {
            return true;
        }

        if (this.pessoaId == null) {
            return false;
        }

        return this.pessoaId.equals(pessoaId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JwtUserDetails that = (JwtUserDetails) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(pessoaId, that.pessoaId) &&
                Objects.equals(numeroDocumento, that.numeroDocumento) &&
                Objects.equals(tipoPessoa, that.tipoPessoa) &&
                Objects.equals(cargo, that.cargo) &&
                Objects.equals(perfil, that.perfil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, pessoaId, numeroDocumento, tipoPessoa, cargo, perfil);
    }

    @Override
    public String toString() {
        return "JwtUserDetails{" +
                "username='" + username + '\'' +
                ", pessoaId=" + pessoaId +
                ", numeroDocumento='" + numeroDocumento + '\'' +
                ", tipoPessoa=" + tipoPessoa +
                ", cargo='" + cargo + '\'' +
                ", perfil=" + perfil +
                '}';
    }
}
