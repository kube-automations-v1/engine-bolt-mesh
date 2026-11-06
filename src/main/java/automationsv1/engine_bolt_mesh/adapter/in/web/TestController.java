package automationsv1.engine_bolt_mesh.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @PostMapping("/reg")
    String registrarPoke(@RequestBody Poke poke){
        return poke.name();
    }

    public record Poke(
            String name
    ){}
}


