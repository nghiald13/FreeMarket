package com.ldn.inventoryservice.catalogue;

import com.ldn.common.annotations.Public;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/inventory/catalogue")
@RequiredArgsConstructor
public class CatalogueController {
    private final CatalogueService catalogueService;

    @GetMapping
    public void findHot25(
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize
    ) {
        this.catalogueService.findHotX(page, pageSize);
    }

}
