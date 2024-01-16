package com.example.emissions.controller;

import com.example.emissions.model.Emission;
import com.example.emissions.model.User;
import com.example.emissions.repository.EmissionRepository;
import com.example.emissions.repository.UserRepository;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(maxAge = 360000)
@RestController
@RequestMapping("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    private final UserRepository userRepository;
    private final EmissionRepository emissionRepository;

    @Autowired
    public UserController(UserRepository userRepository, EmissionRepository emissionRepository) {
        this.userRepository = userRepository;
        this.emissionRepository = emissionRepository;
    }

    @PostMapping("/register")
    public Response registerUser(@RequestBody User user) {
        // Check if the username is already taken
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            // You might want to handle this case differently, e.g., show an error message
            return Response.serverError().entity(user).build();
        }
        // Save the user to the database
        userRepository.save(user);
        // Redirect to the login page
        return Response.ok().entity(user).build();
    }

    @PostMapping("/emission/create")
    public Response createEmission(@RequestBody Emission emission) {
        emissionRepository.save(emission);
        // Redirect to the login page
        return Response.ok().entity(emission).build();
    }

    @PostMapping("/login")
    public Response loginUser(@RequestBody User user) {
        // Check if the username is already taken
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return Response.ok().entity(user).build();
        } else {
            return Response.serverError().entity(user).build();
        }

    }

    @DeleteMapping("/emission/{id}")
    public Response editEmissionForm(@PathVariable Long id) {
        if (emissionRepository.findById(id).isPresent()) {
            emissionRepository.deleteById(id);
            return Response.ok().entity(id).build();
        } else {
            return Response.serverError().entity(id).build();
        }
    }
    @PostMapping("/emission/update/{id}")
    public Response updateEmission(@PathVariable Long id, @RequestBody Emission emission) {

        if (emissionRepository.findById(id).isPresent()) {
            emission.setId(id);
            emissionRepository.save(emission);
            return Response.ok().entity(id).build();
        } else {
            return Response.serverError().entity(id).build();
        }

    }


//    @GetMapping("/edit/{id}")
//    public String editEmissionForm(@PathVariable Long id, Model model) {
//        Emission emission = emissionRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid emission id: " + id));
//
//        model.addAttribute("emission", emission);
//        return "emission/edit";
//    }
    @GetMapping("/emission")
    public Response listEmissions() {
        return Response.ok().entity(emissionRepository.findAll()).build();
    }

}
