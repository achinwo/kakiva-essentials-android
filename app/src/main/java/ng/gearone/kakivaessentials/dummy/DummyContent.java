package ng.gearone.kakivaessentials.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ng.gearone.kakivaessentials.Model;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Model.Product> ITEMS = new ArrayList<>();
    public static final String[] PRODUCT_NAMES = {"Kakiva Co Hair Spray",
            "Kakiva Eventone Cream",
            "Kakiva HRS",
            "Kakiva Hand Sanitizer",
            "Kakiva Salon Shampoo",
            "Kakiva Total Body Lotion",
            "Kakiva conditioner",
            "Kakiva Daisy",
            "Kakiva Face Toner",
            "Kakiva Hair gel",
            "Kakiva O Hair Spray",
            "Kakiva Shampoo",
            "Kakiva White Body Cream",
            "Kakiva face toning wash",
            "Kakiva Edge Control",
            "Kakiva Fresh Body Lotion",
            "Kakiva Hair grow",
            "Kakiva Salon Conditioner",
            "Kakiva Star",
            "Kakiva body wash",
            "kakiva leave-in conditioner"};

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Model.Product> ITEM_MAP = new HashMap<>();

    static {
        // Add some sample items.
        for (int i = 1; i < PRODUCT_NAMES.length; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Model.Product item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Model.Product createDummyItem(int position) {
        Model.Product product = new Model.Product();
        product.details = makeDetails(position);
        product.id = String.valueOf(position);
        product.imageUrl = PRODUCT_NAMES[position].toLowerCase().replace(' ', '_').replace('-', '_');
        product.title = PRODUCT_NAMES[position];
        return product;
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
