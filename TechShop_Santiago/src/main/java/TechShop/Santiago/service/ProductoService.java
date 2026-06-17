/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TechShop.Santiago.service;
import TechShop.Santiago.domain.Producto;
import TechShop.Santiago.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductoService {

    // El repositorio es final para asegurar la inmutabilidad
    private final ProductoRepository categoriaRepository;
    private final FirebaseStorageService firebaseStorageService;

    public ProductoService(ProductoRepository categoriaRepository, FirebaseStorageService firebaseStorageService) {
        this.categoriaRepository = categoriaRepository;
        this.firebaseStorageService = firebaseStorageService;
    }

    @Transactional(readOnly = true)
    public List<Producto> getProductos(boolean activo) {
        if (activo) { //Sólo activos...            
            return categoriaRepository.findByActivoTrue();
        }
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> getProducto(Integer idProducto) {
        return categoriaRepository.findById(idProducto);
    }

    @Transactional
    public void save(Producto categoria, MultipartFile imagenFile) {
        categoria = categoriaRepository.save(categoria);
        if (!imagenFile.isEmpty()) { //Si no está vacío... pasaron una imagen...            
            try {
                String rutaImagen = firebaseStorageService.uploadImage(
                        imagenFile, "producto",
                        categoria.getIdProducto());
                categoria.setRutaImagen(rutaImagen);
                categoriaRepository.save(categoria);
            } catch (IOException e) {

            }
        }
    }

    @Transactional
    public void delete(Integer idProducto) {
        // Verifica si la categoría existe antes de intentar eliminarlo
        if (!categoriaRepository.existsById(idProducto)) {
            // Lanza una excepción para indicar que el usuario no fue encontrado
            throw new IllegalArgumentException("La categoría con ID " + idProducto + " no existe.");
        }
        try {
            categoriaRepository.deleteById(idProducto);
        } catch (DataIntegrityViolationException e) {
            // Lanza una nueva excepción para encapsular el problema de integridad de datos
            throw new IllegalStateException("No se puede eliminar la categoria. Tiene datos asociados.", e);
        }
    }
}



