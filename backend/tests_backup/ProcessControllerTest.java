package com.flowable.platform.unit;

import com.flowable.platform.controller.ProcessController;
import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.service.ProcessService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessController.class)
@ActiveProfiles("test")
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessService processService;

    @Test
    void testStartProcessInstance() throws Exception {
        // Mock process instance
        ProcessInstance mockInstance = new TestProcessInstance("proc-123", "simpleApproval");

        when(processService.startProcessInstance(anyString(), anyMap()))
                .thenReturn(mockInstance);

        mockMvc.perform(post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "processDefinitionKey": "simpleApproval",
                                "variables": {
                                    "employeeName": "John Doe",
                                    "reason": "Leave request"
                                }
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.processInstanceId").value("proc-123"));
    }

    @Test
    void testGetProcessInstance() throws Exception {
        // Mock process instance details
        Map<String, Object> details = new HashMap<>();
        details.put("processInstanceId", "proc-123");
        details.put("processDefinitionKey", "simpleApproval");

        when(processService.getProcessInstanceDetails(anyString()))
                .thenReturn(details);

        mockMvc.perform(get("/api/processes/proc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.processDefinitionKey").value("simpleApproval"));
    }

    @Test
    void testSuspendProcessInstance() throws Exception {
        mockMvc.perform(post("/api/processes/proc-123/suspend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void testActivateProcessInstance() throws Exception {
        mockMvc.perform(post("/api/processes/proc-123/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void testTerminateProcessInstance() throws Exception {
        mockMvc.perform(post("/api/processes/proc-123/terminate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    // Helper class for mocking
    private static class TestProcessInstance implements ProcessInstance {
        private final String id;
        private final String processDefinitionKey;

        TestProcessInstance(String id, String processDefinitionKey) {
            this.id = id;
            this.processDefinitionKey = processDefinitionKey;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getProcessDefinitionKey() {
            return processDefinitionKey;
        }

        @Override
        public String getDeploymentId() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void setDescription(String description) {
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setName(String name) {
        }

        @Override
        public String getBusinessKey() {
            return null;
        }

        @Override
        public void setBusinessKey(String businessKey) {
        }

        @Override
        public boolean isSuspended() {
            return false;
        }

        @Override
        public String getTenantId() {
            return null;
        }

        @Override
        public String getProcessDefinitionId() {
            return null;
        }

        @Override
        public String getActivityId() {
            return null;
        }
    }
}
