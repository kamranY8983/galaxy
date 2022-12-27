package com.kam.galaxy.metabase;

import com.google.common.base.Preconditions;
import com.kam.galaxy.common.exception.GalaxyException;
import com.kam.galaxy.metabase.model.ProductDetails;
import com.kam.galaxy.metabase.model.ProductEntityDetails;
import com.kam.galaxy.metabase.model.TRLAttributes;
import com.kam.galaxy.metabase.payload.ListToStringAdapter;
import com.kam.galaxy.metabase.payload.MetabaseResponseBodyWrapper;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamran Y. Khan
 * @since 25-Dec-2022
 */
public class MetabaseService {

    private  static final Logger log = LoggerFactory.getLogger(MetabaseService.class);
    private  static final String METABASE_URL = "http://localhost:8080/CollibraAPI/";
    private  static final String COMMUNITY_GENESIS = "GENESIS";
    private  static JsonAdapter<MetabaseResponseBodyWrapper> responseJsonAdapter;

    public MetabaseService(){
        final OkHttpClient.Builder httpClientBuilder =
                new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor(log::info).setLevel(HttpLoggingInterceptor.Level.BASIC))
                        .addInterceptor(
                                chain -> {
                                    Request request = chain.request()
                                            .newBuilder()
                                            .addHeader("API_KEY", "XXXXXX")
                                            .build();
                                    return chain.proceed(request);
                                });

        final Moshi moshi = new Moshi.Builder().add(new ListToStringAdapter()).build();

        //this adapter can be used to deserialize json string to ResponseBodyWrapper object
        // used to deserialize error body from the failed response
        responseJsonAdapter =
                moshi.adapter(
                        Types.newParameterizedType(
                                MetabaseResponseBodyWrapper.class, Object.class));

        final Retrofit restClient =
                new Retrofit.Builder()
                        .baseUrl(METABASE_URL)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .client(httpClientBuilder.build())
                        .build();

        api = restClient.create(MetabaseServiceApi.class);
    }

    private final MetabaseServiceApi api;

    private <T> List<T> executeRequest(Call<MetabaseResponseBodyWrapper<T>> request){
        final HttpUrl requestURL = request.request().url();
        try{
            final Response<MetabaseResponseBodyWrapper<T>> response = request.execute();
            if(!response.isSuccessful()){
                final ResponseBody rawErrorBody = response.errorBody();
                if(rawErrorBody == null) {
                    throw new GalaxyException("Metabase request to '%s' failed with status code '%d'"
                            .formatted(requestURL, response.code()));
                }
                else {
                    final  MetabaseResponseBodyWrapper errorBody =
                            responseJsonAdapter.fromJson(rawErrorBody.string());
                    throw  new GalaxyException(
                            "Metabase request to '%s' failed with status code '%d':'%s'"
                                    .formatted(
                                            requestURL,
                                            response.code(),
                                            errorBody == null ? "" : errorBody.message()));
                }
            }
            else {
                final MetabaseResponseBodyWrapper<T> responseBody = response.body();
                return responseBody == null ? List.of() : responseBody.result();
            }
        } catch (IOException ex) {
            throw new GalaxyException("Metabase request to '%s' failed: ".formatted(requestURL), ex);
        }
    }

    public List<ProductDetails> getProductDetails(){
        log.info("Obtaining product details from Metabase");
        final List<ProductDetails> data = executeRequest(api.getProductDetails(null));
        return data == null
                ? Collections.emptyList()
                : data.stream().filter(pd -> pd.sector() != null).collect(Collectors.toList());
    }
    public List<ProductEntityDetails> getProductEntityDetails(String productName){
        Preconditions.checkNotNull(
                productName, "Product name must be specified");
        log.info("Obtaining entity details for product '{}' from Metabase", productName);
        final List<ProductEntityDetails> data = executeRequest(api.getProductEntityDetails(productName));
        return data == null
                ? Collections.emptyList()
                : data;
    }

    public List<TRLAttributes> getTRLAttributes(String trlVersion){
        return getTRLAttributes(null, null, trlVersion);
    }
    public List<TRLAttributes> getTRLAttributes(String productName, String dataEntityName, String trlVersion){
        Preconditions.checkNotNull(
                trlVersion, "TRL version must be specified");
        log.info("Obtaining TRL attributes for product '{}' entity '{}' TRL version '{}' from Metabase",
                productName == null ? "[ALL]" : productName,
                dataEntityName == null ? "[ALL]" : dataEntityName,
                trlVersion);
        final List<TRLAttributes> data = executeRequest(api.getTRLAttributes(COMMUNITY_GENESIS, productName,dataEntityName, trlVersion));
        return data == null
                ? Collections.emptyList()
                : data;
    }

    private interface MetabaseServiceApi {
        @GET("GetProductDetails")
        Call<MetabaseResponseBodyWrapper<ProductDetails>> getProductDetails(@Query("sector") String sector);

        @GET("GetProductEntityDetails")
        Call<MetabaseResponseBodyWrapper<ProductEntityDetails>> getProductEntityDetails(@Query("productName") String productName);

        @GET("GetTRLAttributes")
        Call<MetabaseResponseBodyWrapper<TRLAttributes>> getTRLAttributes(
                @Query("communityName") String communityName,
                @Query("productName") String productName,
                @Query("dataEntityName") String dataEntityName,
                @Query("version") String trlVersion
        );
    }


}
