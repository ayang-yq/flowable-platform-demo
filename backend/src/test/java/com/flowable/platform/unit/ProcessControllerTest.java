package com.flowable.platform.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser")
    void testStartProcessInstance() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", "simpleApproval");
        request.put("variables", Map.of("employeeName", "John Doe", "reason", "Annual leave"));

        mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.processDefinitionKey").value("simpleApproval"))
                .andExpect(jsonPath("$.data.processInstanceId").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testListProcessInstances() throws Exception {
        mockMvc.perform(get("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetProcessInstanceDetails() throws Exception {
        // First start a process
        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", "simpleApproval");
        request.put("variables", Map.of("employeeName", "Jane Doe"));

        String response = mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract processInstanceId from response
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String processInstanceId = (String) data.get("processInstanceId");

        // Now get the details
        mockMvc.perform(get("/api/processes/" + processInstanceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.processInstanceId").value(processInstanceId));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testSuspendProcessInstance() throws Exception {
        // First start a process
        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", "simpleApproval");
        request.put("variables", Map.of("employeeName", "John Doe"));

        String response = mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String processInstanceId = (String) data.get("processInstanceId");

        // Suspend the process
        mockMvc.perform(post("/api/processes/" + processInstanceId + "/suspend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testActivateProcessInstance() throws Exception {
        // First start and suspend a process
        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", "simpleApproval");
        request.put("variables", Map.of("employeeName", "John Doe"));

        String response = mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String processInstanceId = (String) data.get("processInstanceId");

        // Suspend first
        mockMvc.perform(post("/api/processes/" + processInstanceId + "/suspend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Now activate
        mockMvc.perform(post("/api/processes/" + processInstanceId + "/activate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testTerminateProcessInstance() throws Exception {
        // First start a process
        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", "simpleApproval");
        request.put("variables", Map.of("employeeName", "John Doe"));

        String response = mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String processInstanceId = (String) data.get("processInstanceId");

        // Terminate the process
        mockMvc.perform(post("/api/processes/" + processInstanceId + "/terminate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetProcessTasks() throws Exception {
        // First start a process
        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", "simpleApproval");
        request.put("variables", Map.of("employeeName", "John Doe"));

        String response = mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String processInstanceId = (String) data.get("processInstanceId");

        // Get tasks for the process
        mockMvc.perform(get("/api/processes/" + processInstanceId + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testStartProcessWithInvalidKey() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", "nonExistentProcess");
        request.put("variables", Map.of());

        mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetNonExistentProcessInstance() throws Exception {
        mockMvc.perform(get("/api/processes/non-existent-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testProcessVariableSizeLimit() throws Exception {
        // Create a variable larger than 10KB
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 11000; i++) {
            largeValue.append("a");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", "simpleApproval");
        request.put("variables", Map.of("employeeName", "John Doe", "largeData", largeValue.toString()));

        mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
