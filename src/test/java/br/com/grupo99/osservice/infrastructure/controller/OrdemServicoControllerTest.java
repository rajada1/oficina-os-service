package br.com.grupo99.osservice.infrastructure.controller;

import br.com.grupo99.osservice.application.dto.AtualizarStatusRequestDTO;
import br.com.grupo99.osservice.application.dto.OrdemServicoRequestDTO;
import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import br.com.grupo99.osservice.domain.repository.OrdemServicoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrdemServicoController.class, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = br.com.grupo99.osservice.infrastructure.config.SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = br.com.grupo99.osservice.infrastructure.security.jwt.JwtRequestFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = br.com.grupo99.osservice.infrastructure.security.jwt.JwtAuthorizationFilter.class)
}, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("OrdemServicoController - Testes Unitários")
class OrdemServicoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private OrdemServicoRepository repository;

        private static final String BASE_URL = "/api/v1/ordens-servico";

        private OrdemServico criarOSMock() {
                UUID id = UUID.randomUUID();
                UUID clienteId = UUID.randomUUID();
                UUID veiculoId = UUID.randomUUID();

                OrdemServico os = new OrdemServico();
                os.setId(id);
                os.setClienteId(clienteId);
                os.setVeiculoId(veiculoId);
                os.setStatus(StatusOS.RECEBIDA);
                os.setDescricaoProblema("Problema no motor");
                os.setValorTotal(BigDecimal.ZERO);
                os.setDataCriacao(LocalDateTime.now());
                os.setHistorico(new ArrayList<>());
                return os;
        }

        @Test
        @DisplayName("POST - Deve criar uma nova OS com sucesso")
        void deveCriarNovaOS() throws Exception {
                OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                                UUID.randomUUID(), UUID.randomUUID(), "Problema no motor");

                OrdemServico osSalva = criarOSMock();
                when(repository.save(any(OrdemServico.class))).thenReturn(osSalva);

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").isNotEmpty())
                                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                                .andExpect(jsonPath("$.descricaoProblema").value("Problema no motor"));

                verify(repository, times(1)).save(any(OrdemServico.class));
        }

        @Test
        @DisplayName("POST - Deve retornar 400 quando clienteId é nulo")
        void deveRetornar400QuandoClienteIdNulo() throws Exception {
                OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                                null, UUID.randomUUID(), "Problema");

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST - Deve retornar 400 quando veiculoId é nulo")
        void deveRetornar400QuandoVeiculoIdNulo() throws Exception {
                OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                                UUID.randomUUID(), null, "Problema");

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /{id} - Deve buscar OS por ID com sucesso")
        void deveBuscarOSPorId() throws Exception {
                OrdemServico os = criarOSMock();
                when(repository.findById(os.getId())).thenReturn(Optional.of(os));

                mockMvc.perform(get(BASE_URL + "/" + os.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(os.getId().toString()))
                                .andExpect(jsonPath("$.status").value("RECEBIDA"));
        }

        @Test
        @DisplayName("GET /{id} - Deve retornar 404 quando OS não existe")
        void deveRetornar404QuandoOSNaoExiste() throws Exception {
                UUID id = UUID.randomUUID();
                when(repository.findById(id)).thenReturn(Optional.empty());

                mockMvc.perform(get(BASE_URL + "/" + id))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET - Deve listar todas as OS")
        void deveListarTodasOS() throws Exception {
                List<OrdemServico> lista = List.of(criarOSMock(), criarOSMock());
                when(repository.findAll()).thenReturn(lista);

                mockMvc.perform(get(BASE_URL))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("GET - Deve retornar lista vazia quando não há OS")
        void deveRetornarListaVaziaQuandoNaoHaOS() throws Exception {
                when(repository.findAll()).thenReturn(Collections.emptyList());

                mockMvc.perform(get(BASE_URL))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("GET /status/{status} - Deve buscar OS por status")
        void deveBuscarOSPorStatus() throws Exception {
                List<OrdemServico> lista = List.of(criarOSMock());
                when(repository.findByStatus(StatusOS.RECEBIDA)).thenReturn(lista);

                mockMvc.perform(get(BASE_URL + "/status/RECEBIDA"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].status").value("RECEBIDA"));
        }

        @Test
        @DisplayName("PUT /{id}/status - Deve atualizar status da OS")
        void deveAtualizarStatusDaOS() throws Exception {
                OrdemServico os = criarOSMock();
                when(repository.findById(os.getId())).thenReturn(Optional.of(os));

                OrdemServico osAtualizada = criarOSMock();
                osAtualizada.setId(os.getId());
                osAtualizada.setStatus(StatusOS.EM_DIAGNOSTICO);
                when(repository.save(any(OrdemServico.class))).thenReturn(osAtualizada);

                AtualizarStatusRequestDTO request = new AtualizarStatusRequestDTO(
                                StatusOS.EM_DIAGNOSTICO, "Iniciando diagnóstico", "mecanico1");

                mockMvc.perform(put(BASE_URL + "/" + os.getId() + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /{id}/status - Deve retornar 404 quando OS não existe")
        void deveRetornar404AoAtualizarStatusDeOSInexistente() throws Exception {
                UUID id = UUID.randomUUID();
                when(repository.findById(id)).thenReturn(Optional.empty());

                AtualizarStatusRequestDTO request = new AtualizarStatusRequestDTO(
                                StatusOS.EM_DIAGNOSTICO, "Obs", "user");

                mockMvc.perform(put(BASE_URL + "/" + id + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /{id} - Deve deletar OS com sucesso")
        void deveDeletarOS() throws Exception {
                UUID id = UUID.randomUUID();
                when(repository.existsById(id)).thenReturn(true);
                doNothing().when(repository).deleteById(id);

                mockMvc.perform(delete(BASE_URL + "/" + id))
                                .andExpect(status().isNoContent());

                verify(repository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("DELETE /{id} - Deve retornar 404 quando OS não existe")
        void deveRetornar404AoDeletarOSInexistente() throws Exception {
                UUID id = UUID.randomUUID();
                when(repository.existsById(id)).thenReturn(false);

                mockMvc.perform(delete(BASE_URL + "/" + id))
                                .andExpect(status().isNotFound());

                verify(repository, never()).deleteById(any());
        }
}
