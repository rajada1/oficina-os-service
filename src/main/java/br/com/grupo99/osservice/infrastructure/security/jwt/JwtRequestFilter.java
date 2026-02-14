package br.com.grupo99.osservice.infrastructure.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro responsável por interceptar requisições HTTP e validar o token JWT.
 * 
 * Seguindo os princípios:
 * - Single Responsibility: Apenas valida e extrai informações do JWT
 * - Open/Closed: Extensível via configuração, fechado para modificação
 * - Dependency Inversion: Depende de abstrações (JwtUtil)
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Verifica se o header Authorization está presente e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extrai o token removendo o prefixo "Bearer "
            final String jwt = authHeader.substring(BEARER_PREFIX.length());

            // Extrai informações do usuário do token
            final JwtUserDetails userDetails = jwtUtil.extractUserDetails(jwt);

            // Valida o token
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                // Cria o objeto de autenticação
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                // Adiciona detalhes da requisição
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Define a autenticação no contexto do Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.debug("Usuário autenticado: {} com perfil: {}",
                        userDetails.getUsername(),
                        userDetails.getPerfil());
            } else {
                logger.warn("Token JWT inválido para usuário: {}", userDetails.getUsername());
                // Limpa qualquer autenticação existente
                SecurityContextHolder.clearContext();
            }

        } catch (IllegalArgumentException e) {
            // Erro de validação dos dados do token (ex: documento inválido, perfil
            // inválido, etc.)
            logger.error("Erro ao processar token JWT: {}", e.getMessage());
            // Limpa qualquer autenticação existente
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            // Outros erros (ex: token malformado, assinatura inválida, etc.)
            logger.error("Erro ao processar token JWT: {}", e.getMessage(), e);
            // Limpa qualquer autenticação existente
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
