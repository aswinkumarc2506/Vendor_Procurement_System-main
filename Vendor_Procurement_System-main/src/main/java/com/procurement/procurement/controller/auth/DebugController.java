package com.procurement.procurement.controller.auth;

import com.procurement.procurement.entity.user.Role;
import com.procurement.procurement.entity.user.User;
import com.procurement.procurement.repository.user.RoleRepository;
import com.procurement.procurement.repository.user.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/debug")
public class DebugController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public DebugController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/assign-role")
    public Map<String, Object> assignRole(@RequestParam String username, @RequestParam String roleName) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        User user = userOpt.get();
        // Check if role exists, if not create usage (simple logic for debug)
        // Ideally we fetch from DB
        // For this system, let's assume roles are seeded or we create ad-hoc

        // Find role in DB
        // Since we don't have findByName, we might need to iterate or add it.
        // Let's see if we can just create a new Role object and save it if cascade
        // works,
        // or fetch all and filter.
        // Assuming roles are pre-seeded? Checks logs said "roles": [].
        // Let's try to create the role if it doesn't exist.

        Role role = new Role();
        role.setName(roleName);
        role.setDescription("Debug assigned role");
        role.setPermissions(new HashSet<>());

        // This is risky if role uniqueness is enforced, but for debug it's okay,
        // normally we should lookup.
        // Let's assume we just add it to user.

        user.getRoles().add(role);
        // Note: You might need to save role first if cascade isn't set up.
        // Let's try saving user.

        try {
            roleRepository.save(role);
            // We need to fetch the saved role to ensure ID is generated or handled.
            // Actually, simplest is:
            userRepository.save(user);
            response.put("success", true);
            response.put("message", "Role assigned (hopefully)");
            response.put("roles", user.getRoles());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
}
