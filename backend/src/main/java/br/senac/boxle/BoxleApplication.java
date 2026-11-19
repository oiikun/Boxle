package br.senac.boxle;

import br.senac.boxle.services.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.awt.Desktop;
import java.net.URI;

// Classe que liga o sistema. O Spring sobe o back end e serve as telas do Angular, que ficam em resources/static
@SpringBootApplication
public class BoxleApplication implements CommandLineRunner {

	@Autowired
	private DBService dbService;

	public static void main(String[] args) {
		// headless(false) deixa o Java abrir o navegador sozinho
		new SpringApplicationBuilder(BoxleApplication.class).headless(false).run(args);
	}

	@Override
	// Roda logo depois que o Spring liga. Cria os usuários iniciais e abre o navegador no endereço do sistema
	public void run(String... args) throws Exception {
		dbService.instanciarDB();
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI("http://localhost:8080"));
		}
	}
}
