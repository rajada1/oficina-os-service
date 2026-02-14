package br.com.grupo99.osservice.infrastructure.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autorização que valida o acesso baseado em roles.
 * - CLIENTE: acesso limitado a seus próprios serviços
 * - MECANICO/ADMIN: acesso total a todas as operações
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Obtém o token do header
        final String authHeader = request.getHeader("Authorization");

        // Se não houver token, deixa passar (o JwtRequestFilter já tratou a
        // autenticação)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai o token
        String jwt = authHeader.substring(7);

        // Verifica se o usuário está autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai perfil e pessoaId do token
        String perfil = jwtUtil.extractPerfil(jwt);
        String pessoaId = jwtUtil.extractPessoaId(jwt);

        // Se for MECANICO ou ADMIN, permite acesso total
        if ("MECANICO".equals(perfil) || "ADMIN".equals(perfil)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Se for CLIENTE, valida o acesso
        if ("CLIENTE".equals(perfil)) {
            String requestURI = request.getRequestURI();
            String method = request.getMethod();

            // CLIENTE não pode acessar lista completa
            if ("/api/v1/ordens".equals(requestURI) || "/api/v1/os".equals(requestURI)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Acesso negado. Clientes não podem listar todos os itens.\"}");
                return;
            }

            // CLIENTE não pode fazer POST, PUT, DELETE, PATCH
            if (!"GET".equals(method)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter()
                        .write("{\"error\":\"Acesso negado. Clientes só podem consultar ordens de serviço.\"}");
                return;
            }

            // Armazena o pessoaId no request para validação posterior no controller
            request.setAttribute("pessoaId", pessoaId);
            request.setAttribute("perfil", perfil);
        }

        filterChain.doFilter(request, response);
    }
}
