package com.huybq.fund_management.domain.penalty;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PenaltyService {
    private final PenaltyRepository repository;
    private final PenaltyMapper mapper;

    public List<PenaltyDTO> getAllPenalties() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public PenaltyDTO getPenaltyById(Long id) {
        Penalty penalty = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Penalty not found with ID: " + id));
        return mapper.toDTO(penalty);
    }

    public Penalty getPenaltyBySlug(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Penalty not found with name: " + slug));

    }

    public PenaltyDTO createPenalty(@Valid PenaltyDTO penaltyDTO) {
        Penalty penalty = mapper.toEntity(penaltyDTO);
        penalty = repository.save(penalty);
        return mapper.toDTO(penalty);
    }

    public PenaltyDTO updatePenalty(Long id, @Valid PenaltyDTO updatedDTO) {
        return repository.findById(id)
                .map(existingPenalty -> {
                    existingPenalty.setName(updatedDTO.getName());
                    existingPenalty.setDescription(updatedDTO.getDescription());
                    existingPenalty.setAmount(updatedDTO.getAmount());
                    return mapper.toDTO(repository.save(existingPenalty));
                })
                .orElseThrow(() -> new EntityNotFoundException("Penalty not found with ID: " + id));
    }

    public void deletePenalty(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Penalty not found with ID: " + id);
        }
        repository.deleteById(id);
    }
}
