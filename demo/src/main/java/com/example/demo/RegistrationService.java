package com.example.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegistrationService {

    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    @PostConstruct
    public void registerAndSubmit() {
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Prepare registration request headers and body
        HttpHeaders regHeaders = new HttpHeaders();
        regHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> regRequestBody = new HashMap<>();
        regRequestBody.put("name", "Ujjwal sharma");
        regRequestBody.put("regNo", "2210990924");
        regRequestBody.put("email", "ujjwal924.be22@chitkara.edu.in");

        HttpEntity<Map<String, String>> regRequest = new HttpEntity<>(regRequestBody, regHeaders);

        try {
            // Call generateWebhook API to get webhook URL and access token
            ResponseEntity<Map> regResponse = restTemplate.postForEntity(GENERATE_WEBHOOK_URL, regRequest, Map.class);

            if (regResponse.getStatusCode() == HttpStatus.OK && regResponse.getBody() != null) {
                Object webhookObj = regResponse.getBody().get("webhook");
                Object accessTokenObj = regResponse.getBody().get("accessToken");

                if (webhookObj == null || accessTokenObj == null) {
                    System.err.println("Webhook URL or access token is missing in the response.");
                    return;
                }

                String webhookUrl = webhookObj.toString();
                String accessToken = accessTokenObj.toString();

                System.out.println("Webhook URL: " + webhookUrl);
                System.out.println("Access Token: " + accessToken);

                // Step 2: Compose final SQL query for Question 2 (even regNo)
                String finalSql =
                        "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                                "(SELECT COUNT(*) FROM EMPLOYEE e2 WHERE e2.DEPARTMENT = e1.DEPARTMENT AND e2.DOB > e1.DOB) AS YOUNGER_EMPLOYEES_COUNT " +
                                "FROM EMPLOYEE e1 " +
                                "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                                "ORDER BY e1.EMP_ID DESC;";

                // Step 3: Prepare submission headers with bearer token and content type
                HttpHeaders submitHeaders = new HttpHeaders();
                submitHeaders.setContentType(MediaType.APPLICATION_JSON);
                submitHeaders.setBearerAuth(accessToken);

                Map<String, String> submitBody = new HashMap<>();
                submitBody.put("finalQuery", finalSql);

                HttpEntity<Map<String, String>> submitRequest = new HttpEntity<>(submitBody, submitHeaders);

                // Step 4: Submit the SQL query to webhook URL
                ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhookUrl, submitRequest, String.class);

                System.out.println("Submission Status Code: " + submitResponse.getStatusCode());
                System.out.println("Submission Response Body: " + submitResponse.getBody());

            } else {
                System.err.println("Registration failed! HTTP Status: " + regResponse.getStatusCode());
                System.err.println("Response body: " + regResponse.getBody());
            }
        } catch (Exception ex) {
            System.err.println("Exception occurred during registration or submission:");
            ex.printStackTrace();
        }
    }
}
