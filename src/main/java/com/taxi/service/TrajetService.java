package com.taxi.service;

import com.taxi.dto.TrajetRequest;
import com.taxi.dto.TrajetResponse;

public interface TrajetService {
    TrajetResponse publierTrajet(TrajetRequest request);
}
