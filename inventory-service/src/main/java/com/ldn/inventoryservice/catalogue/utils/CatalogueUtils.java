package com.ldn.inventoryservice.catalogue.utils;

import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CatalogueUtils {
    public static String slugify(String s) {
        Slugify slg = Slugify.builder().build();
        return slg.slugify(s);
    }
}
