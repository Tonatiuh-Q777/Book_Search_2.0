package com.desrollador.BookSearch;

import com.desrollador.BookSearch.principal.Principal;
import com.desrollador.BookSearch.repository.LibrosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookSearchApplication implements CommandLineRunner {
	@Autowired
	private LibrosRepository repository;
	public static void main(String[] args) {SpringApplication.run(BookSearchApplication.class, args);}

		@Override
		public void run (String...args) throws Exception {
			Principal principal = new Principal(repository);
			principal.muestraElMenu();
		}
	}

