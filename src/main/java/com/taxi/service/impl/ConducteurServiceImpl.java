package com.taxi.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.taxi.dto.ConducteurRequest;
import com.taxi.dto.ConducteurResponse;
import com.taxi.entity.Conducteur;
import com.taxi.exception.DuplicateResourceException;
import com.taxi.exception.ResourceNotFoundException;
import com.taxi.mapper.ConducteurMapper;
import com.taxi.repository.ConducteurRepository;
import com.taxi.service.ConducteurService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConducteurServiceImpl implements ConducteurService {
    private final ConducteurRepository conducteurRepository;
    private final ConducteurMapper conducteurMapper;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConducteurServiceImpl.class);

    @Override
    public ConducteurResponse create(ConducteurRequest request) {
        if (conducteurRepository.existsByEmail(request.email())) {
            log.warn("Email déjà utilisé: {}", request.email());
            throw new DuplicateResourceException("Cet email est déjà utilisé.");
        }
        if (conducteurRepository.existsByNumeroPermis(request.numeroPermis())) {
            log.warn("Numéro de permis déjà utilisé: {}", request.numeroPermis());
            throw new DuplicateResourceException("Ce numéro de permis est déjà utilisé.");
        }

        Conducteur conducteur = conducteurMapper.toEntity(request);
        Conducteur saved = conducteurRepository.save(conducteur);
        log.info("Conducteur créé id={}", saved.getId());
        return conducteurMapper.toResponse(saved);
    }

    @Override
    public List<ConducteurResponse> findAll() {
        return conducteurRepository.findAll().stream()
                .map(conducteurMapper::toResponse)
                .toList();
    }

    @Override
    public ConducteurResponse findById(Long id) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur non trouvé avec l'id: " + id));
        return conducteurMapper.toResponse(conducteur);
    }

    @Override
    public ConducteurResponse update(Long id, ConducteurRequest request) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur non trouvé avec l'id: " + id));

        if (!conducteur.getEmail().equals(request.email()) && conducteurRepository.existsByEmail(request.email())) {
            log.warn("Email déjà utilisé lors de la mise à jour: {}", request.email());
            throw new DuplicateResourceException("Cet email est déjà utilisé.");
        }
        if (!conducteur.getNumeroPermis().equals(request.numeroPermis())
                && conducteurRepository.existsByNumeroPermis(request.numeroPermis())) {
            log.warn("Numéro de permis déjà utilisé lors de la mise à jour: {}", request.numeroPermis());
            throw new DuplicateResourceException("Ce numéro de permis est déjà utilisé.");
        }

        conducteurMapper.updateEntity(request, conducteur);
        Conducteur updated = conducteurRepository.save(conducteur);
        log.info("Conducteur mis à jour id={}", updated.getId());
        return conducteurMapper.toResponse(updated);
    }

    @Override
    public List<ConducteurResponse> search(String nom, String email) {
        List<Conducteur> resultats;
        if (nom != null && email != null) {
            resultats = conducteurRepository.findByNomContainingIgnoreCaseAndEmailContainingIgnoreCase(nom, email);
        } else if (nom != null) {
            resultats = conducteurRepository.findByNomContainingIgnoreCase(nom);
        } else if (email != null) {
            resultats = conducteurRepository.findByEmailContainingIgnoreCase(email);
        } else {
            resultats = conducteurRepository.findAll();
        }
        return resultats.stream().map(conducteurMapper::toResponse).toList();
    }
}
