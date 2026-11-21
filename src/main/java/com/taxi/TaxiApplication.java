package com.taxi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
// bash ntfada warning li f logs dyale page li katerje3 spring f la recherch
// Serializing PageImpl instances as-is is not supported ... use
// @EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class TaxiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaxiApplication.class, args);
	}

}
