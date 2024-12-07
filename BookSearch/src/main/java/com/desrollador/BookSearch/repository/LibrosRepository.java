package com.desrollador.BookSearch.repository;

import com.desrollador.BookSearch.model.Libros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface LibrosRepository extends JpaRepository<Libros,Long> {

    List<Libros> findByAutorIdGreaterThan(int i);

    List<Libros> findByAutorFechaDeFallecimientoGreaterThanEqualAndAutorFechaDeNacimientoLessThanEqual(String anio, String anio2);

    @Query(value = "SELECT * FROM libros WHERE :idioma = ANY(idiomas)", nativeQuery = true)
    List<Libros> findByIdioma(String idioma);

    boolean existsByTitulo(String titulo);

}
