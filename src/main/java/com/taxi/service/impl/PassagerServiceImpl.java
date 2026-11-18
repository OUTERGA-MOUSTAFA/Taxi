package com.taxi.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.taxi.dto.PassagerRequest;
import com.taxi.dto.PassagerResponse;
import com.taxi.entity.Passager;
import com.taxi.exception.DuplicateResourceException;
import com.taxi.exception.ResourceNotFoundException;
import com.taxi.mapper.PassagerMapper;
import com.taxi.repository.PassagerRepository;
import com.taxi.service.PassagerService;

@Service
public class PassagerServiceImpl implements PassagerService {

    private static final Logger log = LoggerFactory.getLogger(PassagerServiceImpl.class);

    private final PassagerRepository passagerRepository;
    private final PassagerMapper passagerMapper;

    public PassagerServiceImpl(PassagerRepository passagerRepository, PassagerMapper passagerMapper) {
        this.passagerRepository = passagerRepository;
        this.passagerMapper = passagerMapper;
    }

    @Override
    public PassagerResponse create(PassagerRequest request) {
        if (passagerRepository.existsByEmail(request.email())) {
            log.warn("Email déjà utilisé pour le passager: {}", request.email());
            throw new DuplicateResourceException("Cet email est déjà utilisé.");
        }

        Passager passager = passagerMapper.toEntity(request);
        Passager saved = passagerRepository.save(passager);
        log.info("Passager créé id={}", saved.getId());
        return passagerMapper.toResponse(saved);
    }

    @Override
    public List<PassagerResponse> findAll() {
        return passagerRepository.findAll().stream()
                .map(passagerMapper::toResponse)
                .toList();
    }

    @Override
    public PassagerResponse findById(Long id) {
        Passager passager = passagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passager non trouvé avec l'id: " + id));
        return passagerMapper.toResponse(passager);
    }

    @Override
    public PassagerResponse update(Long id, PassagerRequest request) {
        Passager passager = passagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passager non trouvé avec l'id: " + id));

        if (!passager.getEmail().equals(request.email()) && passagerRepository.existsByEmail(request.email())) {
            log.warn("Email déjà utilisé lors de la mise à jour du passager: {}", request.email());
            throw new DuplicateResourceException(
                    "L'email " + request.email() + " est déjà utilisé par un autre passager.");
        }

        passagerMapper.updateEntity(request, passager);
        Passager updated = passagerRepository.save(passager);
        log.info("Passager mis à jour id={}", updated.getId());
        return passagerMapper.toResponse(updated);
    }

    @Override
    public List<PassagerResponse> searchByEmail(String email) {
        return passagerRepository.findByEmailContainingIgnoreCase(email != null ? email : "")
                .stream()
                .map(passagerMapper::toResponse)
                .toList();
    }
}
