package com.desrollador.BookSearch.principal;

import com.desrollador.BookSearch.model.*;
import com.desrollador.BookSearch.repository.LibrosRepository;
import com.desrollador.BookSearch.service.ConsumoAPI;
import com.desrollador.BookSearch.service.ConvierteDatos;
import jakarta.transaction.Transactional;

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
                 buscarLibros();
                 break;
             case 2:
                 mostrarLibrosRegistrados();
                 break;
             case 3:
                 mostrarAutores();
                 break;
             case 4:
                 mostrarAutoresVivos();
                 break;
             case 5:
                 mostrarLibrosPorIdioma();
                 break;
             case 0:
                 System.out.println("Cerrando la aplicación...");
                 break;
             default:
                 System.out.println("Opción invalida");
                 break;
         }
        }
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
            boolean libroYaExiste = repositorio.existsByTitulo(libroBuscado.get().titulo());
            if (libroYaExiste){
                System.out.println("ESTE LIBRO YA FUE REGISTRADO");
                return;
            }
            List<DatosAutor> listaAutores = libroBuscado
                    .map(DatosLibros::autor)
                            .orElse(Collections.emptyList());
            Libros libroNuevo = new Libros();
            libroNuevo.setTitulo(libroBuscado.get().titulo());
            libroNuevo.setIdiomas(libroBuscado.get().idiomas());
            libroNuevo.setNumeroDeDescargas(libroBuscado.get().numeroDeDescargas());
            List<Autores> autoresEntidad = listaAutores.stream()
                    .map(datoAutor -> {
                        Autores autor = new Autores();
                        autor.setNombre(datoAutor.nombre());
                        autor.setFechaDeNacimiento(datoAutor.fechaDeNacimiento());
                        autor.setFechaDeFallecimiento(datoAutor.fechaDeFallecimiento());
                        return autor;
                    })
                    .collect(Collectors.toList());
            libroNuevo.setAutor(autoresEntidad);
            repositorio.save(libroNuevo);

            System.out.println("------LIBRO------" +
                    "\nTitulo: " + libroBuscado.get().titulo() +
                    "\nAutor: " + libroBuscado.get().autor().stream()
                    .map(DatosAutor::nombre)
                    .collect(Collectors.joining(", ")) +
                    "\nIdioma: " + libroBuscado.get().idiomas().stream()
                    .collect(Collectors.joining(", ")) +
                    "\nNumero de descargas: " + libroBuscado.get().numeroDeDescargas() +
                    "\n-----------------");
        }else {
            System.out.println("LIBRO NO ENCONTRADO");
        }
    }
    private void mostrarLibrosRegistrados() {
        libros = repositorio.findAll();
        libros.forEach(l -> System.out.println("------LIBRO------\n" +
                "Titulo: " + l.getTitulo() +
                "\nAutor: " + l.getAutor().stream().map(Autores::getNombre)
                .collect(Collectors.joining(", ")) +
                "\nIdiomas: " + String.join(", ", l.getIdiomas()) +
                "\nTotal descargas: " + l.getNumeroDeDescargas() +
                "\n-----------------"
        ));
    }

    private void mostrarAutores(){
        libros = repositorio.findByAutorIdGreaterThan(0);
        Map<String, List<Libros>> autoresLibrosMap = libros.stream()
                .flatMap(libro -> libro.getAutor().stream()
                        .map(autor -> new AbstractMap.SimpleEntry<>(autor.getNombre(), libro)))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        autoresLibrosMap.forEach((nombreAutor, librosDeAutor) -> {
            Optional<Autores> autorRelacionado = librosDeAutor.stream()
                    .flatMap(libro -> libro.getAutor().stream())
                    .filter(autor -> autor.getNombre().equals(nombreAutor))
                    .findFirst();

            System.out.println("-----------------");
            autorRelacionado.ifPresent(autor -> {
                System.out.println("Autor: " + autor.getNombre());
                System.out.println("Fecha de Nacimiento: " + autor.getFechaDeNacimiento());
                System.out.println("Fecha de Fallecimiento: " + autor.getFechaDeFallecimiento());
            });
            System.out.println("Libros: " + librosDeAutor.stream()
                    .map(Libros::getTitulo)
                    .collect(Collectors.joining(", ")));
            System.out.println("-----------------");
        });

    }
    @Transactional
    private void mostrarAutoresVivos(){
        System.out.println("Digite el año que desea buscar en el cual un autor estaba vivo: ");
        var anio = teclado.nextLine();
        var anio2=anio;
        libros = repositorio.findByAutorFechaDeFallecimientoGreaterThanEqualAndAutorFechaDeNacimientoLessThanEqual(anio,anio2);

        Map<String, List<Libros>> autoresLibrosMap = libros.stream()
                .flatMap(libro -> libro.getAutor().stream()
                        .map(autor -> new AbstractMap.SimpleEntry<>(autor.getNombre(), libro)))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        autoresLibrosMap.forEach((nombreAutor, librosDeAutor) -> {
            Optional<Autores> autorRelacionado = librosDeAutor.stream()
                    .flatMap(libro -> libro.getAutor().stream())
                    .filter(autor -> autor.getNombre().equals(nombreAutor))
                    .findFirst();

            System.out.println("-----------------");
            autorRelacionado.ifPresent(autor -> {
                System.out.println("Autor: " + autor.getNombre());
                System.out.println("Fecha de Nacimiento: " + autor.getFechaDeNacimiento());
                System.out.println("Fecha de Fallecimiento: " + autor.getFechaDeFallecimiento());
            });
            System.out.println("Libros: " + librosDeAutor.stream()
                    .map(Libros::getTitulo)
                    .collect(Collectors.joining(", ")));
            System.out.println("-----------------");
        });
    }
    private void mostrarLibrosPorIdioma(){
        System.out.println("Digite el codigo del idioma que desea buscar los libros:\n" +
                "Ejemplo \n" +
                "es-Español\n" +
                "en-Ingles\n" +
                "pr-Portugues\n");

        var idiomaSelecto = teclado.nextLine();
        libros = repositorio.findByIdioma(idiomaSelecto);
        if (libros.isEmpty()){
            System.out.println("No hay libros encontrados con ese idioma");
        }else {
            libros.forEach(l -> System.out.println("------LIBRO------\n" +
                    "Titulo: " + l.getTitulo() +
                    "\nAutor: " + l.getAutor().stream().map(Autores::getNombre)
                    .collect(Collectors.joining(", ")) +
                    "\nIdiomas: " + String.join(", ", l.getIdiomas()) +
                    "\nTotal descargas: " + l.getNumeroDeDescargas() +
                    "\n-----------------"
            ));
        }
    }
}