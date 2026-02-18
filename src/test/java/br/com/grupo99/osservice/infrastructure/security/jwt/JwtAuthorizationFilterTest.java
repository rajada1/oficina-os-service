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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthorizationFilter")
class JwtAuthorizationFilterTest {

    private JwtAuthorizationFilter jwtAuthorizationFilter;
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

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", JWT_EXPIRATION);

        jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtil);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    private Key getSignInKey() {
        byte[] decodedKey = java.util.Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private String createToken(String perfil) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("pessoaId", PESSOA_ID);
        claims.put("numeroDocumento", NUMERO_DOCUMENTO);
        claims.put("tipoPessoa", TIPO_PESSOA);
        claims.put("perfil", perfil);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private void setAuthenticated() {
        JwtUserDetails userDetails = JwtUserDetails.from(
                USERNAME, PESSOA_ID, NUMERO_DOCUMENTO, TIPO_PESSOA, null, "ADMIN");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Deve passar direto quando não há header Authorization")
    void devePassarDiretoSemAuthorizationHeader() throws ServletException, IOException {
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve passar direto quando header não começa com Bearer")
    void devePassarDiretoSemBearer() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic sometoken");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve passar direto quando usuário não está autenticado")
    void devePassarDiretoQuandoNaoAutenticado() throws ServletException, IOException {
        String token = createToken("ADMIN");
        request.addHeader("Authorization", "Bearer " + token);
        // SecurityContext sem autenticação

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve permitir acesso total para MECANICO")
    void devePermitirAcessoTotalParaMecanico() throws ServletException, IOException {
        String token = createToken("MECANICO");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/ordens");
        request.setMethod("POST");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isNotEqualTo(403);
    }

    @Test
    @DisplayName("Deve permitir acesso total para ADMIN")
    void devePermitirAcessoTotalParaAdmin() throws ServletException, IOException {
        String token = createToken("ADMIN");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/ordens");
        request.setMethod("POST");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isNotEqualTo(403);
    }

    @Test
    @DisplayName("Deve negar acesso a lista completa para CLIENTE em /api/v1/ordens")
    void deveNegarAcessoListaCompletaParaClienteOrdens() throws ServletException, IOException {
        String token = createToken("CLIENTE");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/ordens");
        request.setMethod("GET");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Clientes não podem listar");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve negar acesso a lista completa para CLIENTE em /api/v1/os")
    void deveNegarAcessoListaCompletaParaClienteOs() throws ServletException, IOException {
        String token = createToken("CLIENTE");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/os");
        request.setMethod("GET");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve negar POST para CLIENTE")
    void deveNegarPostParaCliente() throws ServletException, IOException {
        String token = createToken("CLIENTE");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/ordens/123");
        request.setMethod("POST");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Clientes só podem consultar");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve negar PUT para CLIENTE")
    void deveNegarPutParaCliente() throws ServletException, IOException {
        String token = createToken("CLIENTE");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/ordens/123");
        request.setMethod("PUT");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve negar DELETE para CLIENTE")
    void deveNegarDeleteParaCliente() throws ServletException, IOException {
        String token = createToken("CLIENTE");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/ordens/123");
        request.setMethod("DELETE");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve negar PATCH para CLIENTE")
    void deveNegarPatchParaCliente() throws ServletException, IOException {
        String token = createToken("CLIENTE");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/ordens/123");
        request.setMethod("PATCH");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve permitir GET para CLIENTE em endpoint específico e definir atributos")
    void devePermitirGetParaClienteEmEndpointEspecifico() throws ServletException, IOException {
        String token = createToken("CLIENTE");
        request.addHeader("Authorization", "Bearer " + token);
        setAuthenticated();

        request.setRequestURI("/api/v1/ordens/123");
        request.setMethod("GET");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(request.getAttribute("pessoaId")).isEqualTo(PESSOA_ID);
        assertThat(request.getAttribute("perfil")).isEqualTo("CLIENTE");
    }
}
