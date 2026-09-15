package br.com.ibeans.receivables.adapter.in.web.receivable;

import br.com.ibeans.receivables.adapter.in.web.AbstractControllerIT;
import br.com.ibeans.receivables.adapter.out.persistence.receivable.entity.ReceivableEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReceivableControllerIntegrationIT extends AbstractControllerIT {

    private static final String URL = "/api/v1/receivables";
    private static final UUID RECEIVABLE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Nested
    @DisplayName("When Create")
    class WhenCreate {

        @Test
        void deveCadastrarRecebivelEPersistirNoBanco() throws Exception {
            var today = LocalDate.now();

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "assignorId": "CEDENTE-NOVO",
                          "faceValue": 2500.50,
                          "currency": "BRL",
                          "acquisitionDate": "%s",
                          "maturityDate": "%s",
                          "type": "DUPLICATA_MERCANTIL"
                        }
                        """.formatted(today, today.plusDays(90))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.assignorId").value("CEDENTE-NOVO"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.currency").value("BRL"));

            var receivables = receivablesDataBase.findAll();
            assertThat(receivables).hasSize(1);
            assertThat(receivables).extracting(row -> row.get("assignor_id"))
                    .containsExactlyInAnyOrder( "CEDENTE-NOVO");
        }

    }

    @Nested
    @DisplayName("When Find")
    class WhenFind {
        @Test
        void deveConsultarRecebivelCadastradoNosPreRequisitos() throws Exception {
            var receivable = new ReceivableEntity();
            receivable.setId(UUID.fromString("30000000-0000-0000-0000-000000000001"));
            receivable.setAssignorId("CEDENTE-TESTE");
            receivable.setFaceValue(new BigDecimal("1000.00"));
            receivable.setCurrency("BRL");
            receivable.setAcquisitionDate(LocalDate.now());
            receivable.setMaturityDate(receivable.getAcquisitionDate().plusDays(90));
            receivable.setType("DUPLICATA_MERCANTIL");
            receivable.setStatus("PENDING");
            receivable.setCreatedAt(LocalDateTime.now());
            receivable.setUpdatedAt(receivable.getCreatedAt());

            settlementsDataBase.save(receivable);

            mockMvc.perform(get(URL + "/{id}", RECEIVABLE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RECEIVABLE_ID.toString()))
                    .andExpect(jsonPath("$.assignorId").value("CEDENTE-TESTE"))
                    .andExpect(jsonPath("$.faceValue").value("1000.000000"))
                    .andExpect(jsonPath("$.currency").value("BRL"))
                    .andExpect(jsonPath("$.type").value("DUPLICATA_MERCANTIL"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("When Update")
    class WhenUpdate {

        @Test
        void deveAtualizarRecebivelEPersistirAlteracoes() throws Exception {
            var receivable = new ReceivableEntity();
            receivable.setId(UUID.fromString("30000000-0000-0000-0000-000000000001"));
            receivable.setAssignorId("CEDENTE-TESTE");
            receivable.setFaceValue(new BigDecimal("1000.00"));
            receivable.setCurrency("BRL");
            receivable.setAcquisitionDate(LocalDate.now());
            receivable.setMaturityDate(receivable.getAcquisitionDate().plusDays(90));
            receivable.setType("DUPLICATA_MERCANTIL");
            receivable.setStatus("PENDING");
            receivable.setCreatedAt(LocalDateTime.now());
            receivable.setUpdatedAt(receivable.getCreatedAt());

            settlementsDataBase.save(receivable);

            var maturityDate = LocalDate.now().plusDays(120);

            mockMvc.perform(put(URL + "/{id}", RECEIVABLE_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                {
                                  "faceValue": 1800.75,
                                  "maturityDate": "%s",
                                  "type": "CHEQUE_PRE_DATADO"
                                }
                                """.formatted(maturityDate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RECEIVABLE_ID.toString()))
                    .andExpect(jsonPath("$.type").value("CHEQUE_PRE_DATADO"))
                    .andExpect(jsonPath("$.maturityDate").value(maturityDate.toString()));

            mockMvc.perform(get(URL + "/{id}", RECEIVABLE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.faceValue").value("1800.750000"))
                    .andExpect(jsonPath("$.maturityDate").value(maturityDate.toString()))
                    .andExpect(jsonPath("$.type").value("CHEQUE_PRE_DATADO"))
                    .andExpect(jsonPath("$.version").value(1));
        }

    }

}
