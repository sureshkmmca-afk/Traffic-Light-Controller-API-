
package com.example.traffic.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class IntersectionControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @Test
    void create_and_get_state() throws Exception {
        String body = "idint-it-1";
        mvc.perform(post("/api/v1/intersections").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/intersections/int-it-1/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("int-it-1"))
                .andExpect(jsonPath("$.lights.NORTH").exists());
    }

    @Test
    void pause_resume() throws Exception {
        String body = "idint-it-2";
        mvc.perform(post("/api/v1/intersections").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/intersections/int-it-2/commands/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("paused"));

        mvc.perform(post("/api/v1/intersections/int-it-2/commands/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("resumed"));
    }
}
