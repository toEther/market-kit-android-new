package io.horizontalsystems.marketkit.providers

import io.horizontalsystems.marketkit.models.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class HsProvider(
    baseUrl: String,
    oldBaseUrl: String
) {

    private val service by lazy {
        RetrofitUtils.build("${baseUrl}/v1/").create(MarketService::class.java)
    }

    private val serviceOld by lazy {
        RetrofitUtils.build("${oldBaseUrl}/api/v1/").create(MarketServiceOld::class.java)
    }

    fun getFullCoins(): Single<List<FullCoin>> {
        return service.getFullCoins(fullCoinFields)
            .map { responseCoinsList ->
                responseCoinsList.map { it.fullCoin() }
            }
    }

    fun marketInfosSingle(top: Int): Single<List<MarketInfoRaw>> {
        return service.getMarketInfos(marketInfoFields, top)
    }

    fun marketInfosSingle(coinUids: List<String>): Single<List<MarketInfoRaw>> {
        return service.getMarketInfos(marketInfoFields, coinUids.joinToString(","))
    }

    fun marketInfosSingle(categoryUid: String): Single<List<MarketInfoRaw>> {
        return service.getMarketInfosByCategory(marketInfoFields, categoryUid)
    }

    fun getCoinCategories(): Single<List<CoinCategory>> {
        return service.getCategories()
    }

    fun getCoinPrices(coinUids: List<String>, currencyCode: String): Single<List<CoinPrice>> {
        return service.getCoinPrices(
            coinPriceFields,
            coinUids.joinToString(separator = ","),
            currencyCode.lowercase()
        )
            .map { coinPrices ->
                coinPrices.map { coinPriceResponse ->
                    coinPriceResponse.coinPrice(currencyCode)
                }
            }
    }

    fun getMarketInfoOverview(
        coinUid: String,
        currencyCode: String,
        language: String
    ): Single<MarketInfoOverviewRaw> {
        return service.getMarketInfoOverview(coinUid, currencyCode, language)
    }

    fun getGlobalMarketPointsSingle(
        currencyCode: String,
        timePeriod: TimePeriod
    ): Single<List<GlobalMarketPoint>> {
        return serviceOld.globalMarketPoints(timePeriod.v, currencyCode)
    }

    private interface MarketService {
        @GET("coins")
        fun getFullCoins(
            @Query("fields") fields: String
        ): Single<List<FullCoinResponse>>

        @GET("coins")
        fun getMarketInfos(
            @Query("fields") fields: String,
            @Query("limit") top: Int
        ): Single<List<MarketInfoRaw>>

        @GET("coins")
        fun getMarketInfos(
            @Query("fields") fields: String,
            @Query("uids") uids: String,
        ): Single<List<MarketInfoRaw>>

        @GET("categories/{categoryUid}/coins")
        fun getMarketInfosByCategory(
            @Path("categoryUid") categoryUid: String,
            @Query("fields") fields: String,
        ): Single<List<MarketInfoRaw>>

        @GET("categories")
        fun getCategories(): Single<List<CoinCategory>>

        @GET("coins")
        fun getCoinPrices(
            @Query("fields") fields: String,
            @Query("uids") uids: String,
            @Query("currency") currencyCode: String
        ): Single<List<CoinPriceResponse>>

        @GET("coins/{coinUid}")
        fun getMarketInfoOverview(
            @Path("coinUid") coinUid: String,
            @Query("currency") currency: String,
            @Query("language") language: String,
        ): Single<MarketInfoOverviewRaw>
    }

    private interface MarketServiceOld {
        @GET("markets/global/{timePeriod}")
        fun globalMarketPoints(
            @Path("timePeriod") timePeriod: String,
            @Query("currency_code") currencyCode: String
        ): Single<List<GlobalMarketPoint>>
    }

    companion object {
        private val marketInfoFields =
            "name,code,price,price_change_24h,market_cap_rank,coingecko_id,market_cap,total_volume"
        private val fullCoinFields = "name,code,market_cap_rank,coingecko_id,platforms"
        private val coinPriceFields = "price,price_change_24h,last_updated"
    }
}
