package com.taxi.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taxi.dto.VehiculeRequest;
import com.taxi.dto.VehiculeResponse;
import com.taxi.entity.Conducteur;
import com.taxi.entity.Vehicule;
import com.taxi.exception.BusinessRuleException;
import com.taxi.exception.DuplicateResourceException;
import com.taxi.exception.ForbiddenActionException;
import com.taxi.exception.ResourceNotFoundException;
import com.taxi.mapper.VehiculeMapper;
import com.taxi.repository.ConducteurRepository;
import com.taxi.repository.TrajetRepository;
import com.taxi.repository.VehiculeRepository;
import com.taxi.service.VehiculeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private static final Logger log = LoggerFactory.getLogger(VehiculeServiceImpl.class);

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeMapper vehiculeMapper;
    private final TrajetRepository trajetRepository;

    @Override
    @Transactional
    public VehiculeResponse create(VehiculeRequest request) {

        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> {
                    log.warn("Conducteur introuvable lors de la création du véhicule id={}", request.conducteurId());
                    return new ResourceNotFoundException("Conducteur non trouvé avec l'id: " + request.conducteurId());
                });

        if (vehiculeRepository.existsByImmatriculation(request.immatriculation())) {
            log.warn("Immatriculation déjà utilisée: {}", request.immatriculation());
            throw new DuplicateResourceException("Cette immatriculation est déjà utilisée.");
        }

        Vehicule vehicule = vehiculeMapper.toEntity(request);
        vehicule.setConducteur(conducteur);

        Vehicule saved = vehiculeRepository.save(vehicule);
        log.info("Véhicule créé id={}", saved.getId());
        return vehiculeMapper.toResponse(saved);
    }

    // @Override
    // public VehiculeResponse findById(Long id) {
    // Vehicule vehicule = vehiculeRepository.findById(id)
    // .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec
    // l'id: " + id));
    // return vehiculeMapper.toResponse(vehicule);
    // }

    @Override
    @Transactional(readOnly = true) // bash nkhli connexion mefteha hta ila 3eytna 3la toResponse li f mapper itdar
                                    // select hta hewa
    public VehiculeResponse findById(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id: " + id));
        return vehiculeMapper.toResponse(vehicule);
    }

    @Override
    @Transactional
    public VehiculeResponse update(Long id, VehiculeRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(id)// Le véhicule doit exister, sinon 404.
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id: " + id));

        // Le conducteur envoyé dans la requête doit exister, sinon 404.
        // On le vérifie avant la règle de propriété pour que l'erreur
        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> {
                    log.warn("Conducteur introuvable lors de la mise à jour du véhicule id={}", request.conducteurId());
                    return new ResourceNotFoundException("Conducteur non trouvé avec l'id: " + request.conducteurId());
                });

        // Un conducteur ne modifie que ses propres véhicules : on refuse
        // de changer le propriétaire du véhicule (transfert), sinon 403.
        if (!vehicule.getConducteur().getId().equals(request.conducteurId())) {
            log.warn("Tentative de transfert du véhicule id={} vers le conducteur id={}", id, request.conducteurId());
            throw new ForbiddenActionException("Vous ne pouvez modifier que vos propres véhicules.");
        }

        // L'immatriculation est unique. Si elle change, elle ne doit pas
        // déjà appartenir à un autre véhicule, sinon 409.
        // Si elle ne change pas, on ne vérifie rien : sinon le véhicule
        // se trouverait lui-même en doublon.
        if (!vehicule.getImmatriculation().equals(request.immatriculation())
                && vehiculeRepository.existsByImmatriculation(request.immatriculation())) {
            log.warn("Immatriculation déjà utilisée lors de la mise à jour: {}", request.immatriculation());
            throw new DuplicateResourceException("Cette immatriculation est déjà utilisée.");
        }
        vehiculeMapper.updateEntity(request, vehicule);

        Vehicule updated = vehiculeRepository.save(vehicule);
        log.info("Véhicule mis à jour id={}", updated.getId());
        return vehiculeMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> findByConducteurId(Long conducteurId) {
        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResourceNotFoundException("Conducteur non trouvé avec l'id: " + conducteurId);
        }
        return vehiculeRepository.findByConducteurId(conducteurId).stream()
                .map(vehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id: " + id));

        if (trajetRepository.existsByVehiculeId(id)) {
            log.warn("Suppression refusée : véhicule id={} lié à au moins un trajet", id);
            throw new BusinessRuleException("Impossible de supprimer ce véhicule car il est lié à un trajet.");
        }

        vehiculeRepository.delete(vehicule);
        log.info("Véhicule supprimé id={}", id);
    }
}
