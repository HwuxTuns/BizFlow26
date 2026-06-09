package com.example.bizflow.service;

import com.example.bizflow.dto.CreateBranchRequest;
import com.example.bizflow.entity.Branch;
import com.example.bizflow.entity.User;
import com.example.bizflow.repository.BranchRepository;
import com.example.bizflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserRepository userRepository;

    @SuppressWarnings("null")
    public Branch createBranch(CreateBranchRequest request) {
        if (branchRepository.findByName(request.getName()) != null) {
            throw new RuntimeException("Branch name already exists");
        }

        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setEmail(request.getEmail());
        branch.setIsActive(true);

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId()).orElse(null);
            branch.setOwner(owner);
        }

        return branchRepository.save(branch);
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    public Branch getBranchById(@NonNull Long id) {
        return branchRepository.findById(id).orElse(null);
    }

    @SuppressWarnings("null")
    public Branch updateBranch(@NonNull Long id, @NonNull CreateBranchRequest request) {
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new RuntimeException("Branch not found"));
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setEmail(request.getEmail());

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId()).orElse(null);
            branch.setOwner(owner);
        }

        return branchRepository.save(branch);
    }

    public void deleteBranch(@NonNull Long id) {
        branchRepository.deleteById(id);
    }

    // Admin dashboard method
    public long getBranchesCount() {
        return branchRepository.count();
    }
}
