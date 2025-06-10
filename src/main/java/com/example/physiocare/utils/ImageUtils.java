package com.example.physiocare.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;

public class ImageUtils {
    public static Image createImageViewFromBase64(String base64String) {
        if(isBase64(base64String)){
            String cleanedBase64 = cleanBase64String(base64String);

            byte[] imageBytes = Base64.getDecoder().decode(cleanedBase64);

            return new Image(
                    new ByteArrayInputStream(imageBytes),
                    400,
                    400,
                    true,
                    true
            );
        }else {
            try {

                return new Image(base64String, 400, 400, true, true);
            } catch (Exception e) {
                System.err.println("Error al cargar imagen desde ruta: " + e.getMessage());
                return null;
            }
        }

    }

    private static String cleanBase64String(String base64String) {
        // Buscar la posición donde realmente comienza el Base64
        int base64Start = base64String.indexOf("base64,");

        if (base64Start > 0) {
            // Cortar el string desde después de "base64,"
            return base64String.substring(base64Start + 7);
        }
        return base64String;
    }

    private static boolean isBase64(String base64String){
        if (base64String.startsWith("data:image") && base64String.contains("base64,")) {
            return true;
        }

        try {
            Base64.getDecoder().decode(base64String);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
