package com.desrollador.BookSearch.repository;

import com.desrollador.BookSearch.model.Libros;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibrosRepository extends JpaRepository<Libros,Long> {
}
