package com.huybq.fund_management.domain.trans;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trans")
@RequiredArgsConstructor
public class TransController {
    private final TransService transService;


    @GetMapping
    public ResponseEntity<List<TransReponseDTO>> getTrans(){
        return ResponseEntity.ok(transService.getAllTransactions());
    }
}
