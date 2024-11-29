package com.desrollador.BookSearch.principal;

import com.desrollador.BookSearch.model.*;
import com.desrollador.BookSearch.repository.LibrosRepository;
import com.desrollador.BookSearch.service.ConsumoAPI;
import com.desrollador.BookSearch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibrosRepository repositorio;
    private List<Libros> libros;
    String menu = """
            ------------------------
            Elija un opcion de por medio de su numero:
            1- Buscar un libro por titulo
            2- Listar libros registrasdos 
            3- Listar autores registrados
            4- Listar autores vivos en un determinado año
            5- Listar libros por idioma
            0- Salir
            """;

    public Principal(LibrosRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu(){
        int opcion=-1;
        while (opcion!=0){
            System.out.println(menu);
            opcion= teclado.nextInt();
            teclado.nextLine();

         switch (opcion){
             case 1:
                 System.out.println("Cargando...");
                 buscarLibros();
                 break;
             case 2:
                 mostrarLibrosRegistrados();
                 break;
             default:
                 System.out.println("Opción invalida");
                 break;
         }
        }
        System.out.println("Cerrando la aplicación...");
        System.exit(0);
    }
    private void buscarLibros(){
        System.out.println("Ingrese el nombre que desea buscar: ");
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();

        if (libroBuscado.isPresent()){
//            Libros libroNuevo = new Libros();
//            libroNuevo.setTitulo(libroBuscado.get().titulo());
//            libroNuevo.setNumeroDeDescargas(libroBuscado.get().numeroDeDescargas());
//            libroNuevo.setIdiomas(libroBuscado.get().idiomas());
//
            List<DatosAutor> listaAutores = libroBuscado
                    .map(DatosLibros::autor)
                            .orElse(Collections.emptyList());
//            System.out.println("La lista es: "+listaAutores);
//            libroNuevo.setAutor(listaAutores);
//            repositorio.save(libroNuevo);

            Libros libroNuevo = new Libros();
            libroNuevo.setTitulo(libroBuscado.get().titulo());
            libroNuevo.setIdiomas(libroBuscado.get().idiomas());
            libroNuevo.setNumeroDeDescargas(libroBuscado.get().numeroDeDescargas());

            // Transformar DatosAutor a Autores
            List<Autores> autoresEntidad = listaAutores.stream()
                    .map(datoAutor -> {
                        Autores autor = new Autores();
                        autor.setNombre(datoAutor.nombre());
                        autor.setFechaDeNacimiento(datoAutor.fechaDeNacimiento());
                        autor.setFechaDeFallecimiento(datoAutor.fechaDeFallecimiento());
                        return autor;
                    })
                    .collect(Collectors.toList());

            // Asignar autores al libro y guardar
            libroNuevo.setAutor(autoresEntidad);
            repositorio.save(libroNuevo);




            System.out.println("------LIBRO------");
            System.out.println("Titulo: "+libroBuscado.get().titulo());
            System.out.println("Autor: "+libroBuscado.get().autor().stream()
                    .map(DatosAutor::nombre)
                    .collect(Collectors.joining(", ")));
            System.out.println("Idioma: "+libroBuscado.get().idiomas().stream()
                    .collect(Collectors.joining(", ")));
            System.out.println("Numero de descargs: "+libroBuscado.get().numeroDeDescargas()+
                    "\n-----------------");
        }else {
            System.out.println("LIBRO NO ENCONTRADO");
        }
    }
    private void mostrarLibrosRegistrados(){
        libros = repositorio.findAll();
        libros.stream()
                .sorted(Comparator.comparing(Libros::getTitulo))
                .forEach(System.out::println);
    }
}
