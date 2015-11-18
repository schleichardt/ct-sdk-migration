import io.sphere.client.model.SearchResult;
import io.sphere.client.model.products.BackendProductProjection;
import io.sphere.client.shop.CategoryTree;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import io.sphere.internal.ProductConversion;
import io.sphere.sdk.client.HttpRequestIntent;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientFactory;
import io.sphere.sdk.client.SphereRequest;
import io.sphere.sdk.http.HttpResponse;
import io.sphere.sdk.products.search.ProductProjectionSearch;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Main {
    public static final TypeReference<SearchResult<Product>> PRODUCT_SEARCH_RESULT_TYPE_REFERENCE = new TypeReference<SearchResult<Product>>() { };
    public static final TypeReference<SearchResult<BackendProductProjection>> BACKEND_PRODUCT_SEARCH_RESULT_TYPE_REFERENCE = new TypeReference<SearchResult<BackendProductProjection>>() { };

    public static void main(String[] args) throws IOException {
        final SphereClientFactory factory = SphereClientFactory.of();
        try (final SphereClient sphereClient = factory.createClient("tmp-sdk-migration-63", "y5w_LhyCpIaDfKP4QyT8TJLu", "qKOPDecI4j-Ed2KTEECg8MSncVf5A23t")){
            final byte[] bytes = fetchAsBytes(sphereClient, ProductProjectionSearch.ofCurrent());
            final SearchResult<BackendProductProjection> searchResult = parse(bytes, BACKEND_PRODUCT_SEARCH_RESULT_TYPE_REFERENCE);

            //this is only necessary for products since they treated ... "special" in the Play SDK
            final List<Product> products = ProductConversion.fromBackendProductProjections(searchResult.getResults(), getCategoryTree());
            System.err.println(products);
        }
    }

    private static EmptyCategoryTree getCategoryTree() {
        //TODO take here the category from the Play SDK, not this stub
        return new EmptyCategoryTree();
    }

    private static <T> T parse(final byte[] json, final TypeReference<SearchResult<BackendProductProjection>> typeReference) throws IOException {
        /*

        be careful with the imports, Play SDK requires org.codehaus.jackson.map.ObjectMapper
        and the JVM SDK uses com.fasterxml.jackson.databind.ObjectMapper

         */
        final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);//this should be cached
        return objectMapper.reader(typeReference).readValue(json);
    }

    private static byte[] fetchAsBytes(final SphereClient sphereClient, final SphereRequest<?> sphereRequest) {
        final SphereRequest<byte[]> bytesSphereRequest = new SphereRequest<byte[]>() {
            @Override
            public byte[] deserialize(final HttpResponse httpResponse) {
                return httpResponse.getResponseBody();
            }

            @Override
            public HttpRequestIntent httpRequestIntent() {
                return sphereRequest.httpRequestIntent();
            }
        };//deletes type information but keeps request data

        return sphereClient.execute(bytesSphereRequest).toCompletableFuture().join();
    }

    private static class EmptyCategoryTree implements CategoryTree {
        @Override
        public List<Category> getRoots() {
            return Collections.emptyList();
        }

        @Override
        public List<Category> getRoots(final Comparator<Category> comparator) {
            return Collections.emptyList();
        }

        @Override
        public Category getById(final String s) {
            return null;
        }

        @Override
        public Category getBySlug(final String s) {
            return null;
        }

        @Override
        public Category getBySlug(final String s, final Locale locale) {
            return null;
        }

        @Override
        public List<Category> getAsFlatList() {
            return Collections.emptyList();
        }

        @Override
        public void rebuildAsync() {

        }
    }
}