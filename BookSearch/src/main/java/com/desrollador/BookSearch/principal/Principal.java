package com.desrollador.BookSearch.principal;

import com.desrollador.BookSearch.model.Datos;
import com.desrollador.BookSearch.model.DatosAutor;
import com.desrollador.BookSearch.model.DatosLibros;
import com.desrollador.BookSearch.service.ConsumoAPI;
import com.desrollador.BookSearch.service.ConvierteDatos;

import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
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
         }
        }System.exit(0);
    }
    public void buscarLibros(){
        var json = consumoAPI.obtenerDatos(URL_BASE);//System.out.println(json);
        var datos = conversor.obtenerDatos(json, Datos.class);//System.out.println(datos);

        //Busqueda de libros por nombre
        System.out.println("Ingrese el nombre que desea buscar: ");
        var tituloLibro = teclado.nextLine();
        json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();
        if (libroBuscado.isPresent()){
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
}
