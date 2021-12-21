package org.devel.pricetracker.controllers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.devel.pricetracker.AbstractIntegrationMvcTest;
import org.devel.pricetracker.application.dto.ItemPriceDto;
import org.devel.pricetracker.application.utils.Defines;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestIndexApiController extends AbstractIntegrationMvcTest {

    @Test
    void testGetPrices() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(Defines.API_PREFIX + "prices/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        List<ItemPriceDto> itemPriceDtos = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assertions.assertFalse(itemPriceDtos.isEmpty());
    }
}
