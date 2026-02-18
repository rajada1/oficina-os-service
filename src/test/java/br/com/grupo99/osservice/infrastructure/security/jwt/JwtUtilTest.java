package br.com.grupo99.osservice.infrastructure.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET_KEY = "dGVzdC1zZWNyZXQta2V5LXdpdGgtbWluaW11bS0yNTYtYml0cy1mb3ItaHMyNTYtc2lnbmluZw==";
    private static final long JWT_EXPIRATION = 3600000; // 1 hour

    private static final String USERNAME = "testuser@email.com";
    private static final String PESSOA_ID = UUID.randomUUID().toString();
    private static final String NUMERO_DOCUMENTO = "12345678900";
    private static final String TIPO_PESSOA = "FISICA";
    private static final String CARGO = "MECANICO_CHEFE";
    private static final String PERFIL = "ADMIN";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", JWT_EXPIRATION);
    }

    private Key getSignInKey() {
        byte[] decodedKey = java.util.Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private String createTokenWithClaims(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createFullToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("pessoaId", PESSOA_ID);
        claims.put("numeroDocumento", NUMERO_DOCUMENTO);
        claims.put("tipoPessoa", TIPO_PESSOA);
        claims.put("cargo", CARGO);
        claims.put("perfil", PERFIL);
        return createTokenWithClaims(claims, USERNAME, JWT_EXPIRATION);
    }

    @Nested
    @DisplayName("extractUsername")
    class ExtractUsername {

        @Test
        @DisplayName("Deve extrair username do token")
        void deveExtrairUsername() {
            String token = createFullToken();
            String username = jwtUtil.extractUsername(token);
            assertThat(username).isEqualTo(USERNAME);
        }
    }

    @Nested
    @DisplayName("extractPessoaId")
    class ExtractPessoaId {

        @Test
        @DisplayName("Deve extrair pessoaId do token")
        void deveExtrairPessoaId() {
            String token = createFullToken();
            String pessoaId = jwtUtil.extractPessoaId(token);
            assertThat(pessoaId).isEqualTo(PESSOA_ID);
        }

        @Test
        @DisplayName("Deve lançar exceção quando pessoaId ausente")
        void deveLancarExcecaoQuandoPessoaIdAusente() {
            String token = createTokenWithClaims(new HashMap<>(), USERNAME, JWT_EXPIRATION);
            assertThatThrownBy(() -> jwtUtil.extractPessoaId(token))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pessoaId");
        }

        @Test
        @DisplayName("Deve lançar exceção quando pessoaId vazio")
        void deveLancarExcecaoQuandoPessoaIdVazio() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("pessoaId", "  ");
            String token = createTokenWithClaims(claims, USERNAME, JWT_EXPIRATION);
            assertThatThrownBy(() -> jwtUtil.extractPessoaId(token))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pessoaId");
        }
    }

    @Nested
    @DisplayName("extractNumeroDocumento")
    class ExtractNumeroDocumento {

        @Test
        @DisplayName("Deve extrair numero documento do token")
        void deveExtrairNumeroDocumento() {
            String token = createFullToken();
            String doc = jwtUtil.extractNumeroDocumento(token);
            assertThat(doc).isEqualTo(NUMERO_DOCUMENTO);
        }

        @Test
        @DisplayName("Deve lançar exceção quando numeroDocumento ausente")
        void deveLancarExcecaoQuandoNumeroDocumentoAusente() {
            String token = createTokenWithClaims(new HashMap<>(), USERNAME, JWT_EXPIRATION);
            assertThatThrownBy(() -> jwtUtil.extractNumeroDocumento(token))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("numeroDocumento");
        }
    }

    @Nested
    @DisplayName("extractTipoPessoa")
    class ExtractTipoPessoa {

        @Test
        @DisplayName("Deve extrair tipo pessoa do token")
        void deveExtrairTipoPessoa() {
            String token = createFullToken();
            String tipo = jwtUtil.extractTipoPessoa(token);
            assertThat(tipo).isEqualTo(TIPO_PESSOA);
        }

        @Test
        @DisplayName("Deve lançar exceção quando tipoPessoa ausente")
        void deveLancarExcecaoQuandoTipoPessoaAusente() {
            String token = createTokenWithClaims(new HashMap<>(), USERNAME, JWT_EXPIRATION);
            assertThatThrownBy(() -> jwtUtil.extractTipoPessoa(token))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tipoPessoa");
        }
    }

    @Nested
    @DisplayName("extractCargo")
    class ExtractCargo {

        @Test
        @DisplayName("Deve extrair cargo do token")
        void deveExtrairCargo() {
            String token = createFullToken();
            String cargo = jwtUtil.extractCargo(token);
            assertThat(cargo).isEqualTo(CARGO);
        }

        @Test
        @DisplayName("Deve retornar null quando cargo ausente")
        void deveRetornarNullQuandoCargoAusente() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("pessoaId", PESSOA_ID);
            claims.put("perfil", PERFIL);
            String token = createTokenWithClaims(claims, USERNAME, JWT_EXPIRATION);
            String cargo = jwtUtil.extractCargo(token);
            assertThat(cargo).isNull();
        }
    }

    @Nested
    @DisplayName("extractPerfil")
    class ExtractPerfil {

        @Test
        @DisplayName("Deve extrair perfil do token")
        void deveExtrairPerfil() {
            String token = createFullToken();
            String perfil = jwtUtil.extractPerfil(token);
            assertThat(perfil).isEqualTo(PERFIL);
        }

        @Test
        @DisplayName("Deve lançar exceção quando perfil ausente")
        void deveLancarExcecaoQuandoPerfilAusente() {
            String token = createTokenWithClaims(new HashMap<>(), USERNAME, JWT_EXPIRATION);
            assertThatThrownBy(() -> jwtUtil.extractPerfil(token))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("perfil");
        }
    }

    @Nested
    @DisplayName("extractUserDetails")
    class ExtractUserDetails {

        @Test
        @DisplayName("Deve extrair JwtUserDetails completo do token")
        void deveExtrairUserDetailsCompleto() {
            String token = createFullToken();
            JwtUserDetails userDetails = jwtUtil.extractUserDetails(token);

            assertThat(userDetails.getUsername()).isEqualTo(USERNAME);
            assertThat(userDetails.getPessoaId()).isEqualTo(UUID.fromString(PESSOA_ID));
            assertThat(userDetails.getNumeroDocumento()).isEqualTo(NUMERO_DOCUMENTO);
            assertThat(userDetails.getTipoPessoa()).isEqualTo(TIPO_PESSOA);
            assertThat(userDetails.getCargo()).isEqualTo(CARGO);
            assertThat(userDetails.getPerfil()).isEqualTo(PERFIL);
        }
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("Deve gerar token válido a partir de UserDetails")
        void deveGerarTokenValido() {
            JwtUserDetails userDetails = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL);

            String token = jwtUtil.generateToken(userDetails);
            assertThat(token).isNotNull().isNotEmpty();

            String extractedUsername = jwtUtil.extractUsername(token);
            assertThat(extractedUsername).isEqualTo(USERNAME);
        }

        @Test
        @DisplayName("Deve gerar token com claims extras")
        void deveGerarTokenComClaimsExtras() {
            JwtUserDetails userDetails = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL);

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("customClaim", "customValue");

            String token = jwtUtil.generateToken(extraClaims, userDetails);
            assertThat(token).isNotNull().isNotEmpty();

            String extractedUsername = jwtUtil.extractUsername(token);
            assertThat(extractedUsername).isEqualTo(USERNAME);
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("Deve retornar true para token válido")
        void deveRetornarTrueParaTokenValido() {
            JwtUserDetails userDetails = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL);

            String token = jwtUtil.generateToken(userDetails);
            boolean valid = jwtUtil.isTokenValid(token, userDetails);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para token com username diferente")
        void deveRetornarFalseParaUsernameDiferente() {
            JwtUserDetails userDetails = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL);

            String token = jwtUtil.generateToken(userDetails);

            String otherPessoaId = UUID.randomUUID().toString();
            JwtUserDetails otherUser = JwtUserDetails.from(
                    "other@test.com", otherPessoaId, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL);

            boolean valid = jwtUtil.isTokenValid(token, otherUser);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para token expirado")
        void deveRetornarFalseParaTokenExpirado() {
            // Create a token that is already expired
            Map<String, Object> claims = new HashMap<>();
            claims.put("pessoaId", PESSOA_ID);
            claims.put("numeroDocumento", NUMERO_DOCUMENTO);
            claims.put("tipoPessoa", TIPO_PESSOA);
            claims.put("perfil", PERFIL);

            String expiredToken = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(USERNAME)
                    .setIssuedAt(new Date(System.currentTimeMillis() - 7200000))
                    .setExpiration(new Date(System.currentTimeMillis() - 3600000))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();

            JwtUserDetails userDetails = JwtUserDetails.from(
                    USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, CARGO, PERFIL);

            assertThatThrownBy(() -> jwtUtil.isTokenValid(expiredToken, userDetails))
                    .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        }
    }

    @Nested
    @DisplayName("Token inválido")
    class TokenInvalido {

        @Test
        @DisplayName("Deve lançar exceção para token malformado")
        void deveLancarExcecaoParaTokenMalformado() {
            assertThatThrownBy(() -> jwtUtil.extractUsername("invalid.token.here"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para token com assinatura inválida")
        void deveLancarExcecaoParaAssinaturaInvalida() {
            // Create token with different key
            byte[] otherKey = new byte[32];
            java.util.Arrays.fill(otherKey, (byte) 1);
            Key otherSignKey = Keys.hmacShaKeyFor(otherKey);

            String tokenWithDiffKey = Jwts.builder()
                    .setSubject(USERNAME)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                    .signWith(otherSignKey, SignatureAlgorithm.HS256)
                    .compact();

            assertThatThrownBy(() -> jwtUtil.extractUsername(tokenWithDiffKey))
                    .isInstanceOf(Exception.class);
        }
    }
}
