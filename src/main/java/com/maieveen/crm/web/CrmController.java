package com.maieveen.crm.web;

import com.maieveen.crm.user.CrmUser;
import com.maieveen.crm.user.CrmUserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
    public String saveUser(@ModelAttribute("user") CrmUser user, Model model) {
        user.setEmail(user.getEmail() == null ? null : user.getEmail().trim().toLowerCase());
        user.setPhone(user.getPhone() == null || user.getPhone().isBlank() ? null : user.getPhone().trim());

        if (user.getEmail() != null && userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            model.addAttribute("error", "A user with this email address already exists.");
            return "crm/new-user";
        }

        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            model.addAttribute("error", "A user with this phone number already exists.");
            return "crm/new-user";
        }

        userRepository.save(user);
        return "redirect:/crm/admin?saved";
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
    public String userDetails(@PathVariable Long id, Model model) {
        CrmUser user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "crm/user-details";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        CrmUser user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "crm/edit-user";
    }

    @PostMapping("/admin/users/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") CrmUser updatedUser, Model model) {
        CrmUser user = userRepository.findById(id).orElseThrow();
        updatedUser.setEmail(updatedUser.getEmail() == null ? null : updatedUser.getEmail().trim().toLowerCase());
        updatedUser.setPhone(updatedUser.getPhone() == null || updatedUser.getPhone().isBlank() ? null : updatedUser.getPhone().trim());

        if (updatedUser.getEmail() != null && userRepository.existsByEmailIgnoreCaseAndIdNot(updatedUser.getEmail(), id)) {
            model.addAttribute("error", "A user with this email address already exists.");
            model.addAttribute("user", updatedUser);
            return "crm/edit-user";
        }

        if (updatedUser.getPhone() != null && userRepository.existsByPhoneAndIdNot(updatedUser.getPhone(), id)) {
            model.addAttribute("error", "A user with this phone number already exists.");
            model.addAttribute("user", updatedUser);
            return "crm/edit-user";
        }

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setAddress(updatedUser.getAddress());
        user.setSuburb(updatedUser.getSuburb());
        user.setState(updatedUser.getState());
        user.setPostcode(updatedUser.getPostcode());
        userRepository.save(user);
        return "redirect:/crm/admin/users/{id}";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
        return "redirect:/crm/admin/users?deleted";
    }
}
