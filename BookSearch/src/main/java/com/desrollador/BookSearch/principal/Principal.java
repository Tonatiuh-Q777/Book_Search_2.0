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
                 System.out.println("Cargando...");
                 buscarLibros();
                 break;
             case 2:
                 mostrarLibrosRegistrados();
                 break;
             case 3:
                 mostrarAutores();
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
        System.out.println("digite un autor para su busqueda");
        var nombreAutor = teclado.nextLine();
        Optional<Libros> autorBuscado = repositorio.findByAutorNombreContainsIgnoreCase(nombreAutor);

        if (autorBuscado.isPresent()){
            System.out.println("el autor buscado es: " + autorBuscado.get().getAutor().stream().map(Autores::getNombre).collect(Collectors.joining(", ")));
        }else
        {
            System.out.println("autor no encontrado");

        }
    }
}
