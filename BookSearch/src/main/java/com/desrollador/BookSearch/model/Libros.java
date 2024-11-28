package com.desrollador.BookSearch.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class Libros {
    private String titulo;
    private List<DatosAutor> autor;
    private List<String> idiomas;
    private Double numeroDeDescargas;
}
