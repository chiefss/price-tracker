package com.devel.pricetracker.rest.web;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.repository.ItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("tests")
@Sql("/db.tests.data/init.sql")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestIndexWebController {

    @Autowired
    public TestIndexWebController(TestRestTemplate testRestTemplate, ItemRepository itemRepository) {
        this.restTemplate = testRestTemplate;
        this.itemRepository = itemRepository;
    }

    @BeforeAll
    public void setup() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    public void testCreate() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        String itemName = "name test";
        String itemUrl = "url test";
        String itemSelector = "selector test";
        String itemBreakSelector = "break selector test";
        form.set("name", itemName);
        form.set("url", itemUrl);
        form.set("selector", itemSelector);
        form.set("break_selector", itemBreakSelector);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/create", HttpMethod.POST, request, String.class);

        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(6L);
        ItemEntity itemEntity = itemEntityOptional.get();

        Assertions.assertEquals(HttpStatus.FOUND, response.getStatusCode());
        Assertions.assertEquals("/", response.getHeaders().getLocation().getPath());
        Assertions.assertEquals(itemName, itemEntity.getName());
        Assertions.assertEquals(itemUrl, itemEntity.getUrl());
        Assertions.assertEquals(itemSelector, itemEntity.getSelector());
        Assertions.assertEquals(itemBreakSelector, itemEntity.getBreakSelector());
    }

    @Test
    public void testUpdate() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        String itemName = "name test 2-2";
        String itemUrl = "url test 2-2";
        String itemSelector = "selector test 2-2";
        String itemBreakSelector = "break selector test 2-2";
        form.set("id", "2");
        form.set("name", itemName);
        form.set("url", itemUrl);
        form.set("selector", itemSelector);
        form.set("break_selector", itemBreakSelector);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/update", HttpMethod.POST, request, String.class);

        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(2L);
        ItemEntity itemEntity = itemEntityOptional.get();

        Assertions.assertEquals(HttpStatus.FOUND, response.getStatusCode());
        Assertions.assertEquals("/view/2", response.getHeaders().getLocation().getPath());
        Assertions.assertEquals(itemName, itemEntity.getName());
        Assertions.assertEquals(itemUrl, itemEntity.getUrl());
        Assertions.assertEquals(itemSelector, itemEntity.getSelector());
        Assertions.assertEquals(itemBreakSelector, itemEntity.getBreakSelector());
    }

    @Test
    public void testDelete() {
        long id = 3L;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/delete/{id}", HttpMethod.POST, request, String.class, id);

        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(id);

        Assertions.assertEquals(HttpStatus.FOUND, response.getStatusCode());
        Assertions.assertEquals("/", response.getHeaders().getLocation().getPath());
        Assertions.assertTrue(itemEntityOptional.isEmpty());
    }

    @LocalServerPort
    private int port;

    private String baseUrl;

    private final TestRestTemplate restTemplate;

    private final ItemRepository itemRepository;
}
