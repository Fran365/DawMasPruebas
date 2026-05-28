package WebAplicacionesDesarrollo.demo.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoEncontradaException extends RuntimeException {
    public NoEncontradaException(String mensaje) {
        super(mensaje);
    }
    public NoEncontradaException() {
        super("Recurso no encontrado"); // Mensaje por defecto
    }
}
