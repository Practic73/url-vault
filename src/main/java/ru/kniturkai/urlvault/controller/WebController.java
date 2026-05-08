package ru.kniturkai.urlvault.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kniturkai.urlvault.dto.BookmarkRequest;
import ru.kniturkai.urlvault.service.BookmarkService;

import java.util.NoSuchElementException;

@Controller
public class WebController {

    private final BookmarkService service;

    public WebController(BookmarkService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String tag,
                        Model model) {
        model.addAttribute("bookmarks", service.findAll(q, tag));
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("tag", tag != null ? tag : "");
        return "index";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("request", new BookmarkRequest("", "", "", ""));
        return "add";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("request") BookmarkRequest request,
                      BindingResult bindingResult,
                      Model model) {
        if (bindingResult.hasErrors()) {
            return "add";
        }
        service.create(request);
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/";
    }

    @GetMapping("/go/{id}")
    public String go(@PathVariable Long id) {
        String url = service.getUrlById(id);
        return "redirect:" + url;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
