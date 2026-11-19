package br.senac.boxle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Teste padrão que confere se o sistema liga. Usa banco em memória para não mexer nos dados gravados
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testes")
class BoxleApplicationTests {

	@Test
	void contextLoads() {
	}

}
