package br.com.atletahub.atletahub_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource; // Importe esta anotação

@SpringBootTest
@TestPropertySource(properties = "spring.flyway.enabled=false") // >>>>> GARANTA QUE ESTA ANOTAÇÃO ESTEJA PRESENTE <<<<<
class AtletahubBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}