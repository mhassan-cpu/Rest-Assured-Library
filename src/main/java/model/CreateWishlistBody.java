package model;

import com.fasterxml.jackson.databind.ObjectMapper; // ✅ This was missing

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateWishlistBody {

    public static String getCreateWishlistBody(String name, List<Integer> bookIds) {
        try {
            Map<String, Object> wishlistBody = new HashMap<>();
            wishlistBody.put("name", name);
            wishlistBody.put("books", bookIds);


            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(wishlistBody);

        } catch (Exception e) {
            throw new RuntimeException("Failed to build wishlist body JSON", e);
        }
    }
}
