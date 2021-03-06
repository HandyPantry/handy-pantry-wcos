package umm3601.shoppingList;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Map;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.http.NotFoundResponse;
import umm3601.pantry.PantryItem;
import umm3601.product.Product;

/**
 * Controller that manages requests for info about shoppingLists.
 */
public class ShoppingListController {



  /**
   * Construct a controller for shoppingLists.
   *
   * @param database the database containing shoppingList data
   */

  private final JacksonMongoCollection<PantryItem> pantryCollection;
  private final JacksonMongoCollection<Product> productCollection;
  private final JacksonMongoCollection<ShoppingList> shoppingListCollection;

  public ShoppingListController(MongoDatabase database) {
    shoppingListCollection = JacksonMongoCollection.builder().build(
        database,
        "shoppingList",
        ShoppingList.class,
        UuidRepresentation.STANDARD);
    pantryCollection = JacksonMongoCollection.builder().build(
        database,
        "pantry",
        PantryItem.class,
        UuidRepresentation.STANDARD);
    productCollection = JacksonMongoCollection.builder().build(
        database,
        "products",
        Product.class,
        UuidRepresentation.STANDARD);
  }

  /**
   * Get the single shoppingList specified by the `id` parameter in the request.
   *
   * @param ctx a Javalin HTTP context
   */
  public void getShoppingList(Context ctx) {
    String id = ctx.pathParam("id");
    ShoppingList shoppingList;

    try {
      shoppingList = shoppingListCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested shoppingList id wasn't a legal Mongo Object ID.");
    }
    if (shoppingList == null) {
      throw new NotFoundResponse("The requested shoppingList was not found");
    } else {
      ctx.json(shoppingList);
    }
  }

  /**
   * Get a JSON response with a list of all the shoppingLists.
   *
   * @param ctx a Javalin HTTP context
   */
  public void getShoppingLists(Context ctx) {
    Bson combinedFilter = constructFilter(ctx);

    ArrayList<ShoppingList> matchingShoppingLists = shoppingListCollection
        .find(combinedFilter)
        .into(new ArrayList<>());

    // Set the JSON body of the response to be the list of shoppingLists returned by
    // the database.
    ctx.json(matchingShoppingLists);
  }

  private Bson constructFilter(Context ctx) {
    // Combine the list of filters into a single filtering document.
    // Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);
    Bson combinedFilter = new Document();
    return combinedFilter;
  }

  /**
   * Get a JSON response with a list of all the shoppingLists.
   *
   * @param ctx a Javalin HTTP context
   */
  public void addNewShoppingList(Context ctx) {
    ShoppingList newShoppingList = ctx.bodyValidator(ShoppingList.class)
        .check(SLitem -> SLitem.name != null && SLitem.name.length() > 0,
            "ShoppingList must have a non-empty shoppingList name")
        .check(SLitem -> SLitem.quantity > 0,
            "ShoppingList Quantity must be greater than zero")
        .check(SLitem -> SLitem.productID != null && SLitem.productID.length() > 0,
            "ShoppingList must have a non-empty product ID")
        .get();

    shoppingListCollection.insertOne(newShoppingList);

    // 201 is the HTTP code for when we successfully
    // create a new resource (a shoppingList in this case).
    // See, e.g., https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
    // for a description of the various response codes.
    ctx.status(HttpCode.CREATED);
    ctx.json(Map.of("id", newShoppingList._id));
  }

  /**
   * Delete the shoppingList specified by the `id` parameter in the request.
   *
   * @param ctx a Javalin HTTP context
   */
  public void deleteShoppingList(Context ctx) {
    String id = ctx.pathParam("id");
    shoppingListCollection.deleteOne(eq("_id", new ObjectId(id)));
  }

  // the database.
  public void generateShoppingList(Context ctx) {
    ArrayList<PantryItem> pantryItems = pantryCollection
        .find()
        .into(new ArrayList<>());
    ArrayList<Product> products = productCollection
        .find()
        .into(new ArrayList<>());
    Product[] prodArr = products.toArray(new Product[products.size()]);
    PantryItem[] pantryArr = pantryItems.toArray(new PantryItem[pantryItems.size()]);
    Product[] underThresh = new Product[products.size()];
    int[] quants = new int[products.size()];
    int z = 0;
    for (int i = 0; i < prodArr.length; i++) {
      int tempThresh = prodArr[i].threshold;
      String tempID = prodArr[i]._id;
      int count = 0;
      for (int j = 0; j < pantryArr.length; j++) {
        if (pantryArr[j].product.equals(tempID)) {
          count++;
        }
      }
      if (count < tempThresh) {
        underThresh[z] = prodArr[i];
        quants[z] = tempThresh - count;
        z++;
      }
    }
    shoppingListCollection.deleteMany(new Document());
    for (int i = 0; i < z; i++) {
      ShoppingList listItem = new ShoppingList(underThresh[i]._id, underThresh[i].product_name, quants[i]);
      shoppingListCollection.insertOne(listItem);
    }
  }
}
