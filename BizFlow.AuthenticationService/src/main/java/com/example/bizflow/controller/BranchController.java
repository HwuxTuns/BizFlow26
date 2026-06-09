package com.example.bizflow.controller;

import com.example.bizflow.dto.CreateBranchRequest;
import com.example.bizflow.entity.Branch;
import com.example.bizflow.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = "http://localhost:3000")
public class BranchController {
    @Autowired
    private BranchService branchService;

    @PostMapping
    public ResponseEntity<?> createBranch(@RequestBody @NonNull CreateBranchRequest request) {
        try {
            Branch branch = branchService.createBranch(request);
            return ResponseEntity.ok(branch);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Branch>> getAllBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBranchById(@PathVariable @NonNull Long id) {
        Branch branch = branchService.getBranchById(id);
        if (branch != null) {
            return ResponseEntity.ok(branch);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBranch(@PathVariable @NonNull Long id,
            @RequestBody @NonNull CreateBranchRequest request) {
        try {
            Branch branch = branchService.updateBranch(id, request);
            return ResponseEntity.ok(branch);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable @NonNull Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok("Branch deleted successfully");
    }

    // Admin dashboard endpoint - no auth required
    @GetMapping("/count")
    public ResponseEntity<Long> getBranchesCount() {
        return ResponseEntity.ok(branchService.getBranchesCount());
    }
}
