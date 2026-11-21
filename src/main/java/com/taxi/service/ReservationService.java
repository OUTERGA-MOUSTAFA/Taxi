package com.taxi.service;

import com.taxi.dto.ReservationRequest;
import com.taxi.dto.ReservationResponse;

public interface ReservationService {
    ReservationResponse reserver(ReservationRequest request);
}
