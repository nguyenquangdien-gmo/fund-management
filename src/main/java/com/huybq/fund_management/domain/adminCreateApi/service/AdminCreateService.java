package com.huybq.fund_management.domain.adminCreateApi.service;

import com.huybq.fund_management.domain.adminCreateApi.dto.*;
import com.huybq.fund_management.exception.AdminCreateException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminCreateService {
    private final RestTemplate restTemplate;
    @Value("${create-api.base-url}")
    private  String apiBaseUrl;
    @Value("${create-api.auth-header}")
    private  String authHeader;

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + authHeader);

        if (token != null && !token.isEmpty()) {
            headers.set("X-Access-Token", token);
        }

        return headers;
    }

    public ApiResponse<UserDto> signIn(String username, String password) throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/signIn";

            // Create form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("username", username);
            formData.add("password", password);

            HttpHeaders headers = createHeaders(null);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ApiResponse<UserDto>>() {}
            );
            System.out.println(response.getBody());

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Authentication failed";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error during sign in: " + e.getMessage(), e);
        }
    }

    public List<LeaveTypeDto> fetchLeaveTypes(String token) throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/list-staff-attendance";

            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<List<LeaveTypeDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ApiResponse<List<LeaveTypeDto>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Failed to fetch leave types";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error fetching leave types: " + e.getMessage(), e);
        }
    }

    public List<ReporterDto> fetchReporters(String token) throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/authDefault/listReporter";

            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<List<ReporterDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ApiResponse<List<ReporterDto>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Failed to fetch reporters";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error fetching reporters: " + e.getMessage(), e);
        }
    }

    public LeaveRequestDto createLeaveRequest(String token, CreateLeaveRequestDto leaveRequest) throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/auth/staff-attendance/create";


            Map<String, Object> payload = new HashMap<>();
            payload.put("userObjId", leaveRequest.getUserObjId());
            payload.put("staffAttendanceTypeObjId", leaveRequest.getAttendanceTypeObjId());
            payload.put("reportObjId", leaveRequest.getReportObjId());
            payload.put("fromDate", leaveRequest.getFromDate());
            payload.put("endDate", leaveRequest.getEndDate());
            payload.put("note", leaveRequest.getReason());
            payload.put("reason", leaveRequest.getReason());
            payload.put("relationshipObjIds", leaveRequest.getRelationshipObjIds() != null ?
                    leaveRequest.getRelationshipObjIds() : Collections.emptyList());

            HttpHeaders headers = createHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<ApiResponse<LeaveRequestDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ApiResponse<LeaveRequestDto>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Failed to create leave request";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error creating leave request: " + e.getMessage(), e);
        }
    }

    public LeaveRequestDto createWfhRequest(String token, CreateWfhRequestDto wfhRequest) throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/auth/staff-wfh/create";

            HttpHeaders headers = createHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CreateWfhRequestDto> request = new HttpEntity<>(wfhRequest, headers);

            ResponseEntity<ApiResponse<LeaveRequestDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ApiResponse<LeaveRequestDto>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Failed to create WFH request";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error creating WFH request: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> deleteLeaveRequest(String token, String staffAttendanceObjId) throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/auth/staff-attendance/delete";

            HttpHeaders headers = createHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = Collections.singletonMap("staffAttendanceObjId", staffAttendanceObjId);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<ApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Failed to delete leave request";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error deleting leave request: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> deleteWfhRequest(String token, String wfhObjId) throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/auth/staff-wfh/delete";

            HttpHeaders headers = createHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = Collections.singletonMap("wfhObjId", wfhObjId);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<ApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Failed to delete WFH request";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error deleting WFH request: " + e.getMessage(), e);
        }
    }


    public List<LeaveRequestDto> fetchPersonalStaffAttendance(String token, PersonalStaffAttendanceParams params)
            throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/auth/staff-attendance/personalStaffAttendance";

            // Build URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("startDate", params.getStartDate())
                    .queryParam("endDate", params.getEndDate())
                    .queryParam("fromDate", params.getFromDate())
                    .queryParam("toDate", params.getToDate())
                    .queryParam("status", "All")
                    .queryParam("page", params.getPage())
                    .queryParam("userObjId", params.getUserObjId());

            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<PersonalStaffAttendanceResponse>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ApiResponse<PersonalStaffAttendanceResponse>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData().getItems();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Failed to fetch personal staff attendance";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error fetching personal staff attendance: " + e.getMessage(), e);
        }
    }

    public List<LeaveRequestDto> fetchPersonalStaffWfh(String token, PersonalStaffWfhParams params)
            throws AdminCreateException {
        try {
            String url = apiBaseUrl + "/auth/staff-wfh/personalStaffWfh";

            // Build URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("endDate", params.getEndDate())
                    .queryParam("fromDate", params.getFromDate())
                    .queryParam("toDate", params.getToDate())
                    .queryParam("page", params.getPage())
                    .queryParam("status", params.getStatus())
                    .queryParam("userObjId", params.getUserObjId());

            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<PersonalStaffWfhResponse>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ApiResponse<PersonalStaffWfhResponse>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData().getItems();
            } else {
                String errorMessage = response.getBody() != null ?
                        response.getBody().getMessage() : "Failed to fetch personal staff WFH";
                throw new AdminCreateException(errorMessage);
            }
        } catch (Exception e) {
            throw new AdminCreateException("Error fetching personal staff WFH: " + e.getMessage(), e);
        }
    }
}
