package com.desrollador.BookSearch;

import com.desrollador.BookSearch.principal.Principal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookSearchApplication implements CommandLineRunner {

	public static void main(String[] args) {SpringApplication.run(BookSearchApplication.class, args);}

		@Override
		public void run (String...args) throws Exception {
			Principal principal = new Principal();
			principal.muestraElMenu();
		}
	}

