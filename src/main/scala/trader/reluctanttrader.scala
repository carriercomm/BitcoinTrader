import market.Market
import defs._

package trader {
  // A trader that buys immediately if he hasn't bought anything yet.
  // Only sells if he would make a profit, or if it's been a while
  class ReluctantTrader(
      val m: Market,
      var cash: Double,
      var bitcoins: Double,
      val currency: String,
      maxNumUpdates: Int,
      sellPercent: Double)
    extends SingleTrader {

    private var moneyIfSold: Double = 0
    private var moneySpent: Double = 0
    private var idxBought: Int = 0 // = nUpdates when last purchase was made
    private var nUpdates: Int = 0 // number of times update was called
    private val HighThreshold: Double = 1 + sellPercent

    def update(): Unit = {
      val sellQ = sellQuote(bitcoins)
      moneyIfSold = sellQ.dCash
      nUpdates += 1
    }

    def amountToSell: Double = {
      if (bitcoins != 0 &&
          ((nUpdates - idxBought) > maxNumUpdates
          || moneyIfSold > moneySpent * HighThreshold)) {
        return bitcoins
      }
      0.0
    }

    def amountToBuy = {
      if (moneySpent == 0) {
        maxBTCsCanBuy
      } else {
        0.0
      }
    }

    def updateAfterSell(trans: Transaction): Unit = {
      moneySpent = 0
      idxBought = 0
    }

    def updateAfterBuy(trans: Transaction): Unit = {
      moneySpent = -trans.dCash
      idxBought = nUpdates
    }

    def name = "Reluctant Trader"
  }

  class ReluctantTraderFactory(maxNumUpdates: Int, sellPercent: Double)
      extends SingleTraderFactory {
    def newTrader(
        m: Market,
        cash: Double,
        btc: Double,
        currency: String): SingleTrader =
      new ReluctantTrader(m, cash, btc, currency, maxNumUpdates, sellPercent)

    override def toString = "Reluctant Trader Factory"
  }

  object ReluctantTraderFactory {
    def apply(maxNumUpdates: Int, sellPercent: Double) =
      new ReluctantTraderFactory(maxNumUpdates, sellPercent)
  }
}
