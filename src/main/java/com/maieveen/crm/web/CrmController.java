package com.maieveen.crm.web;

import com.maieveen.crm.user.CrmUser;
import com.maieveen.crm.user.CrmUserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/crm")
public class CrmController {

    private final CrmUserRepository userRepository;

    public CrmController(CrmUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String landing() { return "crm/landing"; }

    @GetMapping("/login")
    public String login() { return "crm/login"; }

    @GetMapping("/admin")
    public String admin() { return "crm/admin"; }

    @GetMapping("/admin/new-user")
    public String newUser(Model model) {
        model.addAttribute("user", new CrmUser());
        return "crm/new-user";
    }

    @PostMapping("/admin/new-user")
    public String saveUser(@ModelAttribute("user") CrmUser user) {
        userRepository.save(user);
        return "redirect:/crm/admin/new-user?saved";
    }

    @GetMapping("/admin/users")
    public String existingUsers(@RequestParam(value = "q", required = false, defaultValue = "") String query,
                                Model model) {
        model.addAttribute("query", query);
        model.addAttribute("users", query.isBlank()
                ? userRepository.findAll()
                : userRepository.search(query.trim()));
        return "crm/users";
    }

    @GetMapping("/admin/users/{id}")
    public String userDetails(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        CrmUser user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "crm/user-details";
    }
}
