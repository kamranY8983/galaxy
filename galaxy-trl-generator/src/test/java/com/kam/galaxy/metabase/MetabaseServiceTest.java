package com.kam.galaxy.metabase;

import com.kam.galaxy.metabase.model.ProductDetails;
import com.kam.galaxy.metabase.model.ProductEntityDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 25-Dec-2022
 */
public class MetabaseServiceTest {

    static MetabaseService metabaseService = new MetabaseService();



    @Test
    public void getProductDetailsTest(){
        metabaseService.getProductDetails();
        System.out.println("Success");
    }
    @Test
    public void getTRLAttributesTest(){
        metabaseService.getTRLAttributes("19.0");
        System.out.println("Success");
    }

    public static Stream<Arguments> getAllProductsFromMetabase() {
        return metabaseService.getProductDetails()
                .stream()
                .map(ProductDetails::displayName)
                .map(product -> Arguments.of(product));
    }

    @ParameterizedTest
    @MethodSource("getAllProductsFromMetabase")
    public void getProductEntityDetailsTest(String productName){
        List<ProductEntityDetails> entitiesForProduct = metabaseService.getProductEntityDetails(productName);
    }
}
