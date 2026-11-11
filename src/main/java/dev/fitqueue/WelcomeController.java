package dev.fitqueue;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;


@RestController 
public class WelcomeController {
    
    @GetMapping("/")
    public String welcome(){
        return "Welcome to FitQueue!";
    }

    @GetMapping("/api/info")
    public Map<String, String> info(){
        return Map.of(
            "name", "FitQueue",
            "description", "Fitness class booking and waitlish API"
        );
    }
    
}
