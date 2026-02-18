package br.com.grupo99.osservice.infrastructure.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUserDetails")
class JwtUserDetailsTest {

    private static final String USERNAME = "user@test.com";
    private static final String PESSOA_ID = UUID.randomUUID().toString();
    private static final String NUMERO_DOCUMENTO = "12345678900";
    private static final String TIPO_PESSOA = "FISICA";
    private static final String CARGO = "MECANICO_CHEFE";
    private static final String PERFIL_CLIENTE = "CLIENTE";
    private static final String PERFIL_MECANICO = "MECANICO";
    private static final String PERFIL_ADMIN = "ADMIN";

    @Nested
    @DisplayName("Construtor padrão")
    class ConstrutorPadrao {

        @Test
        @DisplayName("Deve criar instância com valores padrão")
        void deveCriarInstanciaComValoresPadrao() {
            JwtUserDetails details = new JwtUserDetails();

            assertThat(details.getUsername()).isEmpty();
            assertThat(details.getPassword()).isNull();
            assertThat(details.getPessoaId()).isNull();
            assertThat(details.getNumeroDocumento()).isEmpty();
            assertThat(details.getTipoPessoa()).isNull();
            assertThat(details.getCargo()).isNull();
            assertThat(details.getPerfil()).isNull();
            assertThat(details.getAuthorities()).isEmpty();
            assertThat(details.isAccountNonExpired()).isTrue();
            assertThat(details.isAccountNonLocked()).isTrue();
            assertThat(details.isCredentialsNonExpired()).isTrue();
            assertThat(details.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Factory method from()")
    class FactoryMethodFrom {

        @Test
        @DisplayName("Deve criar instância com todos os campos")
        void deveCriarInstanciaComTodosCampos() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);

            assertThat(details.getUsername()).isEqualTo(USERNAME);
            assertThat(details.getPassword()).isNull();
            assertThat(details.getPessoaId()).isEqualTo(UUID.fromString(PESSOA_ID));
            assertThat(details.getNumeroDocumento()).isEqualTo(NUMERO_DOCUMENTO);
            assertThat(details.getTipoPessoa()).isEqualTo(TIPO_PESSOA);
            assertThat(details.getCargo()).isEqualTo(CARGO);
            assertThat(details.getPerfil()).isEqualTo(PERFIL_CLIENTE);
        }

        @Test
        @DisplayName("Deve criar instância com cargo null")
        void deveCriarInstanciaComCargoNull() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, null, PERFIL_CLIENTE);

            assertThat(details.getCargo()).isNull();
        }

        @Test
        @DisplayName("Deve lançar exceção para username nulo")
        void deveLancarExcecaoParaUsernameNulo() {
            assertThatThrownBy(() -> JwtUserDetails.from(
                    null, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Username");
        }

        @Test
        @DisplayName("Deve lançar exceção para pessoaId nulo")
        void deveLancarExcecaoParaPessoaIdNulo() {
            assertThatThrownBy(() -> JwtUserDetails.from(
                    USERNAME, null, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("PessoaId");
        }

        @Test
        @DisplayName("Deve lançar exceção para numeroDocumento nulo")
        void deveLancarExcecaoParaNumeroDocumentoNulo() {
            assertThatThrownBy(() -> JwtUserDetails.from(
                    USERNAME, PESSOA_ID, null, TIPO_PESSOA, CARGO, PERFIL_CLIENTE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("documento");
        }

        @Test
        @DisplayName("Deve lançar exceção para tipoPessoa nulo")
        void deveLancarExcecaoParaTipoPessoaNulo() {
            assertThatThrownBy(() -> JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, null, CARGO, PERFIL_CLIENTE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Tipo de pessoa");
        }

        @Test
        @DisplayName("Deve lançar exceção para perfil nulo")
        void deveLancarExcecaoParaPerfilNulo() {
            assertThatThrownBy(() -> JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Perfil");
        }

        @Test
        @DisplayName("Deve lançar exceção para pessoaId inválido")
        void deveLancarExcecaoParaPessoaIdInvalido() {
            assertThatThrownBy(() -> JwtUserDetails.from(
                    USERNAME, "invalid-uuid", NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PessoaId inválido");
        }
    }

    @Nested
    @DisplayName("Factory method withPassword()")
    class FactoryMethodWithPassword {

        @Test
        @DisplayName("Deve criar instância com senha")
        void deveCriarInstanciaComSenha() {
            JwtUserDetails details = JwtUserDetails.withPassword(
                    USERNAME, "encoded-password", PESSOA_ID, NUMERO_DOCUMENTO,
                    TIPO_PESSOA, CARGO, PERFIL_MECANICO);

            assertThat(details.getUsername()).isEqualTo(USERNAME);
            assertThat(details.getPassword()).isEqualTo("encoded-password");
            assertThat(details.getPerfil()).isEqualTo(PERFIL_MECANICO);
        }

        @Test
        @DisplayName("Deve lançar exceção para password nulo")
        void deveLancarExcecaoParaPasswordNulo() {
            assertThatThrownBy(() -> JwtUserDetails.withPassword(
                    USERNAME, null, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Password");
        }

        @Test
        @DisplayName("Deve lançar exceção para pessoaId inválido em withPassword")
        void deveLancarExcecaoParaPessoaIdInvalidoWithPassword() {
            assertThatThrownBy(() -> JwtUserDetails.withPassword(
                    USERNAME, "pass", "not-a-uuid", NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PessoaId inválido");
        }
    }

    @Nested
    @DisplayName("Authorities")
    class Authorities {

        @Test
        @DisplayName("Deve ter authority ROLE_CLIENTE para perfil CLIENTE")
        void deveTerAuthorityRoleCliente() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);

            Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
            assertThat(authorities).hasSize(1);
            assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_CLIENTE");
        }

        @Test
        @DisplayName("Deve ter authority ROLE_ADMIN para perfil ADMIN")
        void deveTerAuthorityRoleAdmin() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_ADMIN);

            Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
            assertThat(authorities).hasSize(1);
            assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        }
    }

    @Nested
    @DisplayName("Métodos de verificação de perfil")
    class VerificacaoPerfil {

        @Test
        @DisplayName("isMecanicoOrHigher deve retornar true para MECANICO")
        void isMecanicoOrHigherParaMecanico() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_MECANICO);

            assertThat(details.isMecanicoOrHigher()).isTrue();
        }

        @Test
        @DisplayName("isMecanicoOrHigher deve retornar true para ADMIN")
        void isMecanicoOrHigherParaAdmin() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_ADMIN);

            assertThat(details.isMecanicoOrHigher()).isTrue();
        }

        @Test
        @DisplayName("isMecanicoOrHigher deve retornar false para CLIENTE")
        void isMecanicoOrHigherParaCliente() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);

            assertThat(details.isMecanicoOrHigher()).isFalse();
        }

        @Test
        @DisplayName("isCliente deve retornar true para CLIENTE")
        void isClienteParaCliente() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);

            assertThat(details.isCliente()).isTrue();
        }

        @Test
        @DisplayName("isCliente deve retornar false para MECANICO")
        void isClienteParaMecanico() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_MECANICO);

            assertThat(details.isCliente()).isFalse();
        }
    }

    @Nested
    @DisplayName("isOwnerOrMecanico")
    class IsOwnerOrMecanico {

        @Test
        @DisplayName("Deve retornar true para MECANICO independente do pessoaId")
        void deveRetornarTrueParaMecanico() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_MECANICO);

            assertThat(details.isOwnerOrMecanico(UUID.randomUUID())).isTrue();
        }

        @Test
        @DisplayName("Deve retornar true para ADMIN independente do pessoaId")
        void deveRetornarTrueParaAdmin() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_ADMIN);

            assertThat(details.isOwnerOrMecanico(UUID.randomUUID())).isTrue();
        }

        @Test
        @DisplayName("Deve retornar true para CLIENTE quando é o dono")
        void deveRetornarTrueParaClienteDono() {
            UUID pessoaId = UUID.fromString(PESSOA_ID);
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);

            assertThat(details.isOwnerOrMecanico(pessoaId)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para CLIENTE quando não é o dono")
        void deveRetornarFalseParaClienteNaoDono() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);

            assertThat(details.isOwnerOrMecanico(UUID.randomUUID())).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando pessoaId interno é null (construtor padrão)")
        void deveRetornarFalseQuandoPessoaIdNull() {
            JwtUserDetails details = new JwtUserDetails();
            assertThat(details.isOwnerOrMecanico(UUID.randomUUID())).isFalse();
        }
    }

    @Nested
    @DisplayName("equals, hashCode e toString")
    class EqualsHashCodeToString {

        @Test
        @DisplayName("Deve ser igual a si mesmo")
        void deveSerIgualASiMesmo() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);
            assertThat(details).isEqualTo(details);
        }

        @Test
        @DisplayName("Deve ser igual a outro com mesmos valores")
        void deveSerIgualAOutroComMesmosValores() {
            JwtUserDetails details1 = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);
            JwtUserDetails details2 = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);
            assertThat(details1).isEqualTo(details2);
            assertThat(details1.hashCode()).isEqualTo(details2.hashCode());
        }

        @Test
        @DisplayName("Não deve ser igual a outro com username diferente")
        void naoDeveSerIgualComUsernameDiferente() {
            JwtUserDetails details1 = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);
            JwtUserDetails details2 = JwtUserDetails.from(
                    "other@test.com", PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);
            assertThat(details1).isNotEqualTo(details2);
        }

        @Test
        @DisplayName("Não deve ser igual a null")
        void naoDeveSerIgualANull() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);
            assertThat(details).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Não deve ser igual a outro tipo")
        void naoDeveSerIgualAOutroTipo() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);
            assertThat(details).isNotEqualTo("string");
        }

        @Test
        @DisplayName("toString deve conter informações relevantes")
        void toStringDeveConterInformacoes() {
            JwtUserDetails details = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL_CLIENTE);
            String str = details.toString();
            assertThat(str).contains(USERNAME);
            assertThat(str).contains(NUMERO_DOCUMENTO);
            assertThat(str).contains(PERFIL_CLIENTE);
        }
    }
}
