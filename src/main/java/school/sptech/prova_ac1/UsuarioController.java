package school.sptech.prova_ac1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;
    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> buscarTodos() {
        try{
            List<Usuario> usuarios = repository.findAll();
            if (usuarios.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(usuarios);
        }catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody Usuario usuario) {
        try{
            if (repository.existsByCpf(usuario.getCpf()) || repository.existsByEmail(usuario.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            Usuario salvo = repository.save(usuario);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(salvo.getId())
                    .toUri();

            return ResponseEntity.created(location).body(salvo);
        }catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
        try {
            Usuario usuario = repository.findById(id).orElse(null);
            if (usuario == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        try{
            if (!repository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/filtro-data")
    public ResponseEntity<List<Usuario>> buscarPorDataNascimento(@RequestParam LocalDate nascimento) {
        try {
            List<Usuario> usuarios = repository.findByDataNascimentoAfter(nascimento);

            if (usuarios.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(usuarios);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // 500 Internal Server Error
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        try {
            Usuario usuarioAtualizado = repository.findById(id).orElse(null);
            if (usuarioAtualizado == null) {
                return ResponseEntity.notFound().build();
            }

            boolean cpfJaExiste = repository.existsByCpf(usuario.getCpf()) &&
                    !usuarioAtualizado.getCpf().equals(usuario.getCpf());

            boolean emailJaExiste = repository.existsByEmail(usuario.getEmail()) &&
                    !usuarioAtualizado.getEmail().equals(usuario.getEmail());

            if (cpfJaExiste || emailJaExiste) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            usuarioAtualizado.setCpf(usuario.getCpf());
            usuarioAtualizado.setEmail(usuario.getEmail());
            usuarioAtualizado.setNome(usuario.getNome());
            usuarioAtualizado.setSenha(usuario.getSenha());
            usuarioAtualizado.setDataNascimento(usuario.getDataNascimento());

            return ResponseEntity.ok(repository.save(usuarioAtualizado));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
