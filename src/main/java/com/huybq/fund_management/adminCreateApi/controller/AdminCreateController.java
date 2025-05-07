package com.huybq.fund_management.adminCreateApi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huybq.fund_management.adminCreateApi.dto.*;
import com.huybq.fund_management.adminCreateApi.service.AdminCreateService;
import com.huybq.fund_management.exception.AdminCreateException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/${server.version}/admin-create")
@RequiredArgsConstructor
public class AdminCreateController {
    private final AdminCreateService adminCreateService;

    private String getTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AUTHTOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setAuthTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("AUTHTOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Use in production with HTTPS
        response.addCookie(cookie);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password,
                                   HttpServletResponse response) {
        try {
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Username and password are required"
                ));
            }

            ApiResponse<UserDto> apiResponse = adminCreateService.signIn(username, password);

            if (apiResponse.isSuccess()) {
                // Set auth token in cookie
                setAuthTokenCookie(response, apiResponse.getToken());

                log.info("User {}",apiResponse.getToken());
                // Return user data without sensitive information
                UserDto user = apiResponse.getData();

                // Set user data in separate cookie for frontend use
                Map<String, Object> userData = new HashMap<>();
                userData.put("userObjId", user.getUserObjId());
                userData.put("name", user.getName());
                userData.put("staffCode", user.getStaffCode());
                userData.put("userPositionCode", user.getUserPositionCode());
                userData.put("departmentCode", user.getDepartmentCode());

                ObjectMapper objectMapper = new ObjectMapper();
                String userJson = objectMapper.writeValueAsString(userData);
                String encodedUser = Base64.getEncoder().encodeToString(userJson.getBytes());

                Cookie userCookie = new Cookie("user", encodedUser);
                userCookie.setPath("/");
                response.addCookie(userCookie);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", user
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", apiResponse.getMessage()
                ));
            }
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("AUTHTOKEN", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Cookie userCookie = new Cookie("user", null);
        userCookie.setPath("/");
        userCookie.setMaxAge(0);
        response.addCookie(userCookie);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
        ));
    }

    @GetMapping("/leave-types")
    public ResponseEntity<?> getLeaveTypes(HttpServletRequest request) {
        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        try {
            List<LeaveTypeDto> leaveTypes = adminCreateService.fetchLeaveTypes(token);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", leaveTypes
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/reporters")
    public ResponseEntity<?> getReporters(HttpServletRequest request) {
        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        try {
            List<ReporterDto> reporters = adminCreateService.fetchReporters(token);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", reporters
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }


    @PostMapping("/leave/create")
    public ResponseEntity<?> createLeaveRequest(HttpServletRequest request,
                                                @RequestBody CreateLeaveRequestDto leaveRequest) {
        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        try {
            LeaveRequestDto createdRequest = adminCreateService.createLeaveRequest(token, leaveRequest);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", createdRequest
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/wfh/create")
    public ResponseEntity<?> createWfhRequest(HttpServletRequest request,
                                              @RequestBody CreateWfhRequestDto wfhRequest) {
        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        try {
            LeaveRequestDto createdRequest = adminCreateService.createWfhRequest(token, wfhRequest);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", createdRequest
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/leave/delete")
    public ResponseEntity<?> deleteLeaveRequest(HttpServletRequest request,
                                                @RequestBody Map<String, String> requestBody) {
        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        String staffAttendanceObjId = requestBody.get("staffAttendanceObjId");
        if (staffAttendanceObjId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "staffAttendanceObjId is required"
            ));
        }

        try {
            Map<String, Object> result = adminCreateService.deleteLeaveRequest(token, staffAttendanceObjId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/wfh/delete")
    public ResponseEntity<?> deleteWfhRequest(HttpServletRequest request,
                                              @RequestBody Map<String, String> requestBody) {
        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        String wfhObjId = requestBody.get("wfhObjId");
        if (wfhObjId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "wfhObjId is required"
            ));
        }

        try {
            Map<String, Object> result = adminCreateService.deleteWfhRequest(token, wfhObjId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/my-wfh-requests")
    public ResponseEntity<?> getMyWfhRequests(HttpServletRequest request) {
        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        try {
            List<LeaveRequestDto> wfhRequests = adminCreateService.fetchMyWfhRequests(token);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", wfhRequests
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/personal-staff-attendance")
    public ResponseEntity<?> getPersonalStaffAttendance(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam Integer fromDate,
            @RequestParam Integer toDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam String userObjId) {

        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        try {
            PersonalStaffAttendanceParams params = new PersonalStaffAttendanceParams();
            params.setStartDate(startDate);
            params.setEndDate(endDate);
            params.setFromDate(fromDate);
            params.setToDate(toDate);
            params.setPage(page);
            params.setUserObjId(userObjId);

            List<LeaveRequestDto> attendanceRecords = adminCreateService.fetchPersonalStaffAttendance(token, params);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", attendanceRecords
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/personal-staff-wfh")
    public ResponseEntity<?> getPersonalStaffWfh(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String fromDate,
            @RequestParam Integer toDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam String userObjId) {

        String token = getTokenFromCookies(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
            ));
        }

        try {
            PersonalStaffWfhParams params = new PersonalStaffWfhParams();
            params.setStartDate(startDate);
            params.setEndDate(endDate);
            params.setFromDate(fromDate);
            params.setToDate(toDate);
            params.setPage(page);
            params.setUserObjId(userObjId);

            List<LeaveRequestDto> wfhRecords = adminCreateService.fetchPersonalStaffWfh(token, params);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", wfhRecords
            ));
        } catch (AdminCreateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
