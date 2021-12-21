package org.devel.pricetracker.controllers.web;

import org.devel.pricetracker.AbstractIntegrationMvcTest;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.entities.ItemPrice;
import org.devel.pricetracker.application.repository.ItemDao;
import org.devel.pricetracker.application.repository.ItemPriceDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestIndexWebController extends AbstractIntegrationMvcTest {

    private final ItemDao itemDao;

    private final ItemPriceDao itemPriceDao;

    @Autowired
    TestIndexWebController(ItemDao itemDao, ItemPriceDao itemPriceDao) {
        this.itemDao = itemDao;
        this.itemPriceDao = itemPriceDao;
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testViewById() throws Exception {
        mockMvc.perform(get("/view/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreate() throws Exception {
        String itemName = "name test";
        String itemUrl = "url test";
        String itemSelector = "selector test";
        String itemBreakSelector = "break selector test";

        mockMvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", itemName)
                        .param("url", itemUrl)
                        .param("selector", itemSelector)
                        .param("break_selector", itemBreakSelector)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/view/*"));

        Optional<Item> itemEntityOptional = itemDao.findById(6L);
        Item item = itemEntityOptional.get();
        Assertions.assertEquals(itemName, item.getName());
        Assertions.assertEquals(itemUrl, item.getUrl());
        Assertions.assertEquals(itemSelector, item.getSelector());
        Assertions.assertEquals(itemBreakSelector, item.getBreakSelector());
    }

    @Test
    void testUpdate() throws Exception {
        String itemName = "name test 2-2";
        String itemUrl = "url test 2-2";
        String itemSelector = "selector test 2-2";
        String itemBreakSelector = "break selector test 2-2";

        mockMvc.perform(post("/update")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "2")
                        .param("name", itemName)
                        .param("url", itemUrl)
                        .param("selector", itemSelector)
                        .param("break_selector", itemBreakSelector)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/2"));

        Optional<Item> itemEntityOptional = itemDao.findById(2L);
        Item item = itemEntityOptional.get();
        Assertions.assertEquals(itemName, item.getName());
        Assertions.assertEquals(itemUrl, item.getUrl());
        Assertions.assertEquals(itemSelector, item.getSelector());
        Assertions.assertEquals(itemBreakSelector, item.getBreakSelector());
    }

    @Test
    void testDelete() throws Exception {
        long id = 3L;

        mockMvc.perform(post("/delete/{id}", id)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Optional<Item> itemEntityOptional = itemDao.findById(id);
        Assertions.assertTrue(itemEntityOptional.isEmpty());
    }

    @Test
    void testActivate() throws Exception {
        long id = 2L;

        mockMvc.perform(post("/activate/{id}", id)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testDeactivate() throws Exception {
        long id = 2L;

        mockMvc.perform(post("/deactivate/{id}", id)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testPricesCleanall() throws Exception {
        mockMvc.perform(get("/prices/cleanall")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testClean() throws Exception {
        long id = 4L;

        mockMvc.perform(get("/prices/clean/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/" + id));

        Item item = new Item();
        item.setId(id);
        List<ItemPrice> itemPriceEntities = itemPriceDao.findAllByItemId(item.getId(), null);
        Assertions.assertEquals(6, itemPriceEntities.size());
    }
}
