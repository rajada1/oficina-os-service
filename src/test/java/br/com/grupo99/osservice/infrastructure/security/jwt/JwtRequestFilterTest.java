package br.com.grupo99.osservice.infrastructure.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtRequestFilter")
class JwtRequestFilterTest {

    private JwtRequestFilter jwtRequestFilter;
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String SECRET_KEY = "dGVzdC1zZWNyZXQta2V5LXdpdGgtbWluaW11bS0yNTYtYml0cy1mb3ItaHMyNTYtc2lnbmluZw==";
    private static final long JWT_EXPIRATION = 3600000;
    private static final String USERNAME = "testuser@email.com";
    private static final String PESSOA_ID = UUID.randomUUID().toString();
    private static final String NUMERO_DOCUMENTO = "12345678900";
    private static final String TIPO_PESSOA = "FISICA";
    private static final String PERFIL = "ADMIN";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", JWT_EXPIRATION);

        jwtRequestFilter = new JwtRequestFilter(jwtUtil);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    private Key getSignInKey() {
        byte[] decodedKey = java.util.Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private String createValidToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("pessoaId", PESSOA_ID);
        claims.put("numeroDocumento", NUMERO_DOCUMENTO);
        claims.put("tipoPessoa", TIPO_PESSOA);
        claims.put("perfil", PERFIL);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("Deve continuar filtro sem autenticação quando header Authorization ausente")
    void deveContinuarFiltroSemAuthorizationHeader() throws ServletException, IOException {
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve continuar filtro quando header não começa com Bearer")
    void deveContinuarFiltroSemBearer() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic sometoken");

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve autenticar com token válido")
    void deveAutenticarComTokenValido() throws ServletException, IOException {
        String token = createValidToken();
        request.addHeader("Authorization", "Bearer " + token);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();

        JwtUserDetails principal = (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        assertThat(principal.getUsername()).isEqualTo(USERNAME);
        assertThat(principal.getPerfil()).isEqualTo(PERFIL);
    }

    @Test
    @DisplayName("Deve limpar contexto para token malformado")
    void deveLimparContextoParaTokenMalformado() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer invalid.token.here");

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve limpar contexto para token expirado")
    void deveLimparContextoParaTokenExpirado() throws ServletException, IOException {
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

        request.addHeader("Authorization", "Bearer " + expiredToken);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve limpar contexto quando token não tem claims obrigatórios")
    void deveLimparContextoQuandoTokenSemClaims() throws ServletException, IOException {
        // Token without required claims like pessoaId, perfil
        String tokenSemClaims = Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        request.addHeader("Authorization", "Bearer " + tokenSemClaims);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        // Should clear context because extractUserDetails will throw IllegalArgumentException
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve limpar contexto para token com assinatura inválida")
    void deveLimparContextoParaAssinaturaInvalida() throws ServletException, IOException {
        byte[] otherKey = new byte[32];
        java.util.Arrays.fill(otherKey, (byte) 1);
        Key otherSignKey = Keys.hmacShaKeyFor(otherKey);

        String badToken = Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(otherSignKey, SignatureAlgorithm.HS256)
                .compact();

        request.addHeader("Authorization", "Bearer " + badToken);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
