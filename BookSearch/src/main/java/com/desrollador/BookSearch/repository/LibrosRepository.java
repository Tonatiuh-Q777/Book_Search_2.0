package com.desrollador.BookSearch.repository;

import com.desrollador.BookSearch.model.Autores;
import com.desrollador.BookSearch.model.Libros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibrosRepository extends JpaRepository<Libros,Long> {

    Optional<Libros> findByAutorNombreContainsIgnoreCase(String nombreAutor);

}
